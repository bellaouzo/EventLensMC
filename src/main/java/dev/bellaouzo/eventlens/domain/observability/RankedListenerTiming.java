package dev.bellaouzo.eventlens.domain.observability;

import dev.bellaouzo.eventlens.domain.trace.ListenerIdentity;
import org.jspecify.annotations.NonNull;

public record RankedListenerTiming(
        @NonNull ListenerIdentity identity,
        DurationStats stats,
        int invocationCount,
        boolean frequentlyInvoked,
        boolean mainThreadBlocked) {}
