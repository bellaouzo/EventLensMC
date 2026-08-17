package dev.bellaouzo.eventlens.domain.plugin;

import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

public record PluginTraceStatistics(
        @NonNull DurationStats listenerTiming,
        int invocationCount,
        int tracedDispatchCount,
        int exceptionCount,
        @NonNull Map<String, Integer> exceptionCountByType,
        @NonNull List<PluginAttributedChange> recentChanges,
        @NonNull Map<String, Integer> traceCoInteractions,
        boolean agentAttached) {

    public static PluginTraceStatistics empty(boolean agentAttached) {
        return new PluginTraceStatistics(DurationStats.empty(), 0, 0, 0, Map.of(), List.of(), Map.of(), agentAttached);
    }
}
