package dev.bellaouzo.eventlens.domain.report;

import java.util.List;
import java.util.Map;

public record TraceRegressionData(
        String sourceId,
        String eventClassName,
        String sessionState,
        int capturedDispatches,
        int droppedDispatches,
        String filterDescription,
        int warningCount,
        int dispatchCount,
        long totalDurationNanos,
        int cancelledAtEndCount,
        int partialDispatchCount,
        Map<String, Integer> pluginDispatchCounts,
        Map<String, Integer> pluginInvocationCounts,
        List<String> dispatchFingerprints) {

    public TraceRegressionData {
        sourceId = sourceId == null ? "unknown" : sourceId;
        eventClassName = eventClassName == null ? "" : eventClassName;
        sessionState = sessionState == null ? "" : sessionState;
        filterDescription = filterDescription == null ? "" : filterDescription;
        pluginDispatchCounts = pluginDispatchCounts == null ? Map.of() : Map.copyOf(pluginDispatchCounts);
        pluginInvocationCounts = pluginInvocationCounts == null ? Map.of() : Map.copyOf(pluginInvocationCounts);
        dispatchFingerprints = dispatchFingerprints == null ? List.of() : List.copyOf(dispatchFingerprints);
    }
}
