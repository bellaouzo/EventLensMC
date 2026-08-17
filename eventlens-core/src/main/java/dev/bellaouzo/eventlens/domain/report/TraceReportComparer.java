package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TraceReportComparer {

    private TraceReportComparer() {}

    public static TraceRegressionReport compare(TraceReportDocument left, TraceReportDocument right) {
        return compare(left, right, Optional.empty());
    }

    public static TraceRegressionReport compare(
            TraceReportDocument left, TraceReportDocument right, Optional<String> pluginName) {
        return compare(toRegressionData(left, pluginName), toRegressionData(right, pluginName));
    }

    public static TraceRegressionReport compare(TraceRegressionData left, TraceRegressionData right) {
        List<String> differences = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        boolean sameEvent = left.eventClassName().equals(right.eventClassName());
        if (!sameEvent) {
            differences.add("Event class differs: " + left.eventClassName() + " vs " + right.eventClassName());
        }
        if (!left.sessionState().equals(right.sessionState())) {
            differences.add("State: " + left.sessionState() + " vs " + right.sessionState());
        }
        if (left.capturedDispatches() != right.capturedDispatches()) {
            differences.add("Captured dispatches: " + left.capturedDispatches() + " vs " + right.capturedDispatches());
        }
        if (left.droppedDispatches() != right.droppedDispatches()) {
            differences.add("Dropped dispatches: " + left.droppedDispatches() + " vs " + right.droppedDispatches());
        }
        if (!left.filterDescription().equals(right.filterDescription())) {
            differences.add("Filters differ: " + left.filterDescription() + " vs " + right.filterDescription());
        }

        if (left.dispatchCount() != right.dispatchCount()) {
            differences.add("Dispatch records: " + left.dispatchCount() + " vs " + right.dispatchCount());
        }

        long leftAvgNanos = left.dispatchCount() == 0 ? 0L : left.totalDurationNanos() / left.dispatchCount();
        long rightAvgNanos = right.dispatchCount() == 0 ? 0L : right.totalDurationNanos() / right.dispatchCount();
        if (leftAvgNanos != rightAvgNanos) {
            differences.add("Avg dispatch ms: " + formatMillis(leftAvgNanos) + " vs " + formatMillis(rightAvgNanos));
        }

        if (left.cancelledAtEndCount() != right.cancelledAtEndCount()) {
            differences.add("Cancelled-at-end: " + left.cancelledAtEndCount() + " vs " + right.cancelledAtEndCount());
        }

        if (left.partialDispatchCount() != right.partialDispatchCount()) {
            differences.add(
                    "Partial dispatches: " + left.partialDispatchCount() + " vs " + right.partialDispatchCount());
        }

        if (!left.pluginInvocationCounts().equals(right.pluginInvocationCounts())) {
            differences.add("Per-plugin invocation distribution differs.");
        }

        if (!left.dispatchFingerprints().equals(right.dispatchFingerprints())) {
            notes.add("Dispatch fingerprint sequence differs ("
                    + left.dispatchFingerprints().size() + " vs "
                    + right.dispatchFingerprints().size() + ").");
        }

        if (left.warningCount() != right.warningCount()) {
            notes.add("Warning count differs (" + left.warningCount() + " vs " + right.warningCount() + ").");
        }
        if (!left.pluginDispatchCounts().equals(right.pluginDispatchCounts())) {
            notes.add("Dispatch coverage per plugin changed.");
        }

        return new TraceRegressionReport(
                left.sourceId(), right.sourceId(), "all dispatches", sameEvent, differences, notes);
    }

    public static TraceRegressionData toRegressionData(TraceReportDocument document, Optional<String> pluginScope) {
        TraceSessionSummary summary = document.summary();
        List<TraceDispatchRecord> scopedDispatches = filterDispatches(document.dispatches(), pluginScope);

        Map<String, Integer> pluginDispatchCounts = new LinkedHashMap<>();
        Map<String, Integer> pluginInvocationCounts = new LinkedHashMap<>();
        List<String> fingerprints = new ArrayList<>();

        long totalDurationNanos = 0L;
        int cancelledAtEndCount = 0;
        int partialDispatchCount = 0;

        for (TraceDispatchRecord dispatch : scopedDispatches) {
            totalDurationNanos += dispatch.durationNanos();
            if (dispatch.cancelledAtEnd()) {
                cancelledAtEndCount++;
            }
            if (!dispatch.partialReasons().isEmpty()) {
                partialDispatchCount++;
            }

            List<TraceListenerSnapshot> listeners = dispatch.listenerChain();
            for (TraceListenerSnapshot listener : listeners) {
                pluginInvocationCounts.merge(listener.pluginName(), 1, Integer::sum);
            }
            listeners.stream()
                    .map(TraceListenerSnapshot::pluginName)
                    .distinct()
                    .forEach(plugin -> pluginDispatchCounts.merge(plugin, 1, Integer::sum));

            fingerprints.add(dispatchFingerprint(dispatch));
        }

        String sourceId = summary.sessionId();
        if (pluginScope.isPresent()) {
            sourceId = sourceId + " (plugin=" + pluginScope.get() + ")";
        }
        return new TraceRegressionData(
                sourceId,
                summary.eventClassName(),
                summary.state().name(),
                summary.capturedEvents(),
                summary.droppedEvents(),
                TraceFilterFormatter.describe(document.filter()),
                document.warnings().size(),
                scopedDispatches.size(),
                totalDurationNanos,
                cancelledAtEndCount,
                partialDispatchCount,
                pluginDispatchCounts,
                pluginInvocationCounts,
                fingerprints);
    }

    private static List<TraceDispatchRecord> filterDispatches(
            List<TraceDispatchRecord> dispatches, Optional<String> pluginScope) {
        if (pluginScope.isEmpty()) {
            return dispatches;
        }
        String wanted = pluginScope.get();
        return dispatches.stream()
                .filter(dispatch -> dispatch.listenerChain().stream()
                        .anyMatch(listener -> listener.pluginName().equalsIgnoreCase(wanted)))
                .toList();
    }

    private static String dispatchFingerprint(TraceDispatchRecord dispatch) {
        String plugins = dispatch.listenerChain().stream()
                .map(TraceListenerSnapshot::pluginName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .reduce((left, right) -> left + "," + right)
                .orElse("-");
        return dispatch.eventClassName() + "|"
                + dispatch.cancelledAtStart() + "->" + dispatch.cancelledAtEnd() + "|"
                + dispatch.partialReasons().size() + "|"
                + plugins;
    }

    private static String formatMillis(long nanos) {
        return String.format(java.util.Locale.ROOT, "%.3f", nanos / 1_000_000.0);
    }
}
