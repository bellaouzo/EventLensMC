package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.diff.BandChange;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.observability.DurationStatsCalculator;
import dev.bellaouzo.eventlens.domain.plugin.PluginAttributedChange;
import dev.bellaouzo.eventlens.domain.plugin.PluginTraceStatistics;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class PluginTraceAggregator {

    private static final int MAX_RECENT_CHANGES = 10;

    private PluginTraceAggregator() {}

    static PluginTraceStatistics aggregate(
            String pluginName, TraceSessionManager traceSessionManager, boolean agentAttached) {
        if (traceSessionManager == null) {
            return PluginTraceStatistics.empty(agentAttached);
        }

        List<Long> durations = new ArrayList<>();
        int exceptionCount = 0;
        Map<String, Integer> exceptionCountByType = new HashMap<>();
        List<PluginAttributedChange> recentChanges = new ArrayList<>();
        Map<String, Integer> traceCoInteractions = new HashMap<>();
        int tracedDispatchCount = 0;

        for (TraceSessionDetail detail : listSessionDetails(traceSessionManager)) {
            for (TraceDispatchRecord dispatch : detail.records()) {
                if (!isPluginInvolved(pluginName, dispatch)) {
                    continue;
                }

                tracedDispatchCount++;
                collectTimings(pluginName, dispatch, durations, exceptionCountByType);
                exceptionCount += countExceptions(pluginName, dispatch);
                collectCoInteractions(pluginName, dispatch, traceCoInteractions);
                collectChanges(pluginName, detail.summary().sessionId(), dispatch, recentChanges);
            }
        }

        recentChanges.sort(Comparator.comparingLong(PluginAttributedChange::dispatchSequence)
                .reversed());
        if (recentChanges.size() > MAX_RECENT_CHANGES) {
            recentChanges = recentChanges.subList(0, MAX_RECENT_CHANGES);
        }

        return new PluginTraceStatistics(
                DurationStatsCalculator.compute(durations),
                durations.size(),
                tracedDispatchCount,
                exceptionCount,
                Map.copyOf(exceptionCountByType),
                List.copyOf(recentChanges),
                Map.copyOf(traceCoInteractions),
                agentAttached);
    }

    private static List<TraceSessionDetail> listSessionDetails(TraceSessionManager traceSessionManager) {
        return traceSessionManager.listSessions().stream()
                .map(summary -> traceSessionManager.getSessionDetail(summary.sessionId()))
                .flatMap(Optional::stream)
                .toList();
    }

    private static boolean isPluginInvolved(String pluginName, TraceDispatchRecord dispatch) {
        if (dispatch.listenerChain().stream().anyMatch(listener -> matchesPlugin(pluginName, listener.pluginName()))) {
            return true;
        }
        return dispatch.listenerTimings().stream().anyMatch(timing -> matchesPlugin(pluginName, timing.pluginName()));
    }

    private static void collectTimings(
            String pluginName,
            TraceDispatchRecord dispatch,
            List<Long> durations,
            Map<String, Integer> exceptionCountByType) {
        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            if (!matchesPlugin(pluginName, timing.pluginName())) {
                continue;
            }
            durations.add(timing.durationNanos());
            if (timing.threwException()) {
                timing.exceptionType().ifPresent(type -> exceptionCountByType.merge(type, 1, Integer::sum));
            }
        }
    }

    private static int countExceptions(String pluginName, TraceDispatchRecord dispatch) {
        int count = 0;
        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            if (matchesPlugin(pluginName, timing.pluginName()) && timing.threwException()) {
                count++;
            }
        }
        return count;
    }

    private static void collectCoInteractions(
            String pluginName, TraceDispatchRecord dispatch, Map<String, Integer> traceCoInteractions) {
        for (TraceListenerSnapshot listener : dispatch.listenerChain()) {
            if (!matchesPlugin(pluginName, listener.pluginName())) {
                traceCoInteractions.merge(listener.pluginName(), 1, Integer::sum);
            }
        }
    }

    private static void collectChanges(
            String pluginName,
            String sessionId,
            TraceDispatchRecord dispatch,
            List<PluginAttributedChange> recentChanges) {
        for (BandChange bandChange : TraceDispatchAnalyzer.bandChanges(dispatch, false)) {
            if (isAttributedToPlugin(pluginName, bandChange)) {
                List<String> changedProperties = bandChange.diff().changed().stream()
                        .map(PropertyChange::property)
                        .toList();
                if (!changedProperties.isEmpty()) {
                    recentChanges.add(new PluginAttributedChange(
                            sessionId,
                            dispatch.eventClassName(),
                            dispatch.sequence(),
                            bandChange.priorityBand(),
                            changedProperties,
                            bandChange.conflictingAttribution()));
                }
            }
        }
    }

    private static boolean isAttributedToPlugin(String pluginName, BandChange bandChange) {
        return bandChange.attributedPlugins().stream().anyMatch(name -> matchesPlugin(pluginName, name));
    }

    private static boolean matchesPlugin(String expected, String actual) {
        return actual.toLowerCase(Locale.ROOT).equals(expected.toLowerCase(Locale.ROOT));
    }
}
