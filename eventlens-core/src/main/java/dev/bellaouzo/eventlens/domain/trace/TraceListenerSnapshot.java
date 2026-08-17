package dev.bellaouzo.eventlens.domain.trace;

import org.jspecify.annotations.NonNull;

public record TraceListenerSnapshot(
        int registrationOrder,
        @NonNull String pluginName,
        @NonNull String listenerClassName,
        @NonNull String methodName,
        @NonNull String priority,
        boolean ignoreCancelled) {}
