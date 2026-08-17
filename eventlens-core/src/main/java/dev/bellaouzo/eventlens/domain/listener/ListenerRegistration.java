package dev.bellaouzo.eventlens.domain.listener;

import org.jspecify.annotations.NonNull;

public record ListenerRegistration(
        int registrationOrder,
        @NonNull String pluginName,
        @NonNull String listenerClassName,
        @NonNull String methodName,
        @NonNull String priority,
        boolean ignoreCancelled) {}
