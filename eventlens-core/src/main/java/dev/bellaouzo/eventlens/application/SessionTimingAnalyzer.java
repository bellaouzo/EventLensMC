package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.observability.DurationStatsCalculator;
import dev.bellaouzo.eventlens.domain.observability.PerformanceBudget;
import dev.bellaouzo.eventlens.domain.observability.RankedListenerTiming;
import dev.bellaouzo.eventlens.domain.observability.RankedPluginTiming;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.trace.ListenerIdentity;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SessionTimingAnalyzer {

    private static final int TOP_RANKINGS = 5;

    private SessionTimingAnalyzer() {}

    public static SessionTimingSummary analyze(
            List<TraceDispatchRecord> records, int sampledOutEvents, long slowThresholdNanos, boolean agentAttached) {
        if (records.isEmpty()) {
            return new SessionTimingSummary(
                    DurationStats.empty(),
                    DurationStats.empty(),
                    List.of(),
                    List.of(),
                    List.of(),
                    sampledOutEvents,
                    agentAttached ? Set.of() : Set.of(TracePartialReason.AGENT_ABSENT),
                    slowThresholdNanos,
                    agentAttached);
        }

        List<Long> dispatchDurations = new ArrayList<>();
        List<Long> overheadDurations = new ArrayList<>();
        Map<ListenerIdentity, List<Long>> listenerDurations = new HashMap<>();
        Map<ListenerIdentity, Integer> listenerCounts = new HashMap<>();
        Map<String, List<Long>> pluginDurations = new HashMap<>();
        Map<String, Integer> pluginCounts = new HashMap<>();
        EnumSet<TracePartialReason> sessionPartialReasons = EnumSet.noneOf(TracePartialReason.class);

        for (TraceDispatchRecord dispatch : records) {
            dispatchDurations.add(dispatch.durationNanos());
            overheadDurations.add(dispatch.eventLensOverheadNanos());
            sessionPartialReasons.addAll(dispatch.partialReasons());

            for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
                ListenerIdentity identity =
                        new ListenerIdentity(timing.pluginName(), timing.listenerClassName(), timing.methodName());
                listenerDurations
                        .computeIfAbsent(identity, ignored -> new ArrayList<>())
                        .add(timing.durationNanos());
                listenerCounts.merge(identity, 1, Integer::sum);
                pluginDurations
                        .computeIfAbsent(timing.pluginName(), ignored -> new ArrayList<>())
                        .add(timing.durationNanos());
                pluginCounts.merge(timing.pluginName(), 1, Integer::sum);
            }
        }

        if (agentAttached) {
            sessionPartialReasons.remove(TracePartialReason.AGENT_ABSENT);
        } else {
            sessionPartialReasons.add(TracePartialReason.AGENT_ABSENT);
        }

        double medianListenerRate = computeMedianListenerRate(listenerCounts, records.size());
        List<RankedListenerTiming> slowestListeners = listenerDurations.entrySet().stream()
                .map(entry -> {
                    ListenerIdentity identity = entry.getKey();
                    DurationStats stats = DurationStatsCalculator.compute(entry.getValue());
                    int count = listenerCounts.getOrDefault(identity, 0);
                    double rate = records.isEmpty() ? 0.0 : (double) count / records.size();
                    boolean frequentlyInvoked = medianListenerRate > 0.0
                            && rate > medianListenerRate * PerformanceBudget.FREQUENT_LISTENER_MULTIPLIER;
                    boolean mainThreadBlocked = entry.getValue().stream()
                            .anyMatch(duration -> duration >= PerformanceBudget.MAIN_THREAD_BLOCK_NANOS);
                    return new RankedListenerTiming(identity, stats, count, frequentlyInvoked, mainThreadBlocked);
                })
                .sorted(Comparator.comparingLong(
                                (RankedListenerTiming ranked) -> ranked.stats().p95Nanos())
                        .thenComparingLong(ranked -> ranked.stats().maxNanos())
                        .reversed())
                .limit(TOP_RANKINGS)
                .toList();

        List<RankedPluginTiming> slowestPlugins = pluginDurations.entrySet().stream()
                .map(entry -> new RankedPluginTiming(
                        entry.getKey(),
                        DurationStatsCalculator.compute(entry.getValue()),
                        pluginCounts.getOrDefault(entry.getKey(), 0)))
                .sorted(Comparator.comparingLong(
                                (RankedPluginTiming ranked) -> ranked.stats().p95Nanos())
                        .thenComparingLong(ranked -> ranked.stats().maxNanos())
                        .reversed())
                .limit(TOP_RANKINGS)
                .toList();

        List<String> frequentListenerWarnings = slowestListeners.stream()
                .filter(RankedListenerTiming::frequentlyInvoked)
                .map(ranked -> ranked.identity().displayName() + " invoked " + ranked.invocationCount() + " times")
                .toList();

        return new SessionTimingSummary(
                DurationStatsCalculator.compute(dispatchDurations),
                DurationStatsCalculator.compute(overheadDurations),
                slowestListeners,
                slowestPlugins,
                frequentListenerWarnings,
                sampledOutEvents,
                Set.copyOf(sessionPartialReasons),
                slowThresholdNanos,
                agentAttached);
    }

    private static double computeMedianListenerRate(Map<ListenerIdentity, Integer> listenerCounts, int dispatchCount) {
        if (dispatchCount == 0 || listenerCounts.isEmpty()) {
            return 0.0;
        }
        List<Double> rates = listenerCounts.values().stream()
                .map(count -> (double) count / dispatchCount)
                .sorted()
                .toList();
        int middle = rates.size() / 2;
        if (rates.size() % 2 == 1) {
            return rates.get(middle);
        }
        return (rates.get(middle - 1) + rates.get(middle)) / 2.0;
    }

    public static List<ListenerTimingRecord> slowListenersForDispatch(
            TraceDispatchRecord dispatch, long slowThresholdNanos) {
        return dispatch.listenerTimings().stream()
                .filter(timing -> timing.durationNanos() >= slowThresholdNanos || timing.exceedsSlowThreshold())
                .sorted(Comparator.comparingLong(ListenerTimingRecord::durationNanos)
                        .reversed())
                .toList();
    }

    public static DurationStats listenerStatsForDispatch(TraceDispatchRecord dispatch) {
        List<Long> durations = dispatch.listenerTimings().stream()
                .map(ListenerTimingRecord::durationNanos)
                .toList();
        return DurationStatsCalculator.compute(durations);
    }
}
