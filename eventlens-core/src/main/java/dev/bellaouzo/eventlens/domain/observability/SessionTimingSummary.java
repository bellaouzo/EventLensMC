package dev.bellaouzo.eventlens.domain.observability;

import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.List;
import java.util.Set;

public record SessionTimingSummary(
        DurationStats dispatchStats,
        DurationStats eventLensOverheadStats,
        List<RankedListenerTiming> slowestListeners,
        List<RankedPluginTiming> slowestPlugins,
        List<String> frequentListenerWarnings,
        int sampledOutEvents,
        Set<TracePartialReason> sessionPartialReasons,
        long slowThresholdNanos,
        boolean agentAttached) {}
