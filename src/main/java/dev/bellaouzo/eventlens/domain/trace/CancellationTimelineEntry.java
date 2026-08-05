package dev.bellaouzo.eventlens.domain.trace;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;

public record CancellationTimelineEntry(
        int invocationOrder,
        String pluginName,
        String listenerClassName,
        String methodName,
        CancellationTransitionKind kind,
        boolean cancelledBefore,
        boolean cancelledAfter) {}
