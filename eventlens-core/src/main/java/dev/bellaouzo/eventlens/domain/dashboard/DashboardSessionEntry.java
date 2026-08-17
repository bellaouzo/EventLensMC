package dev.bellaouzo.eventlens.domain.dashboard;

import org.jspecify.annotations.NonNull;

public record DashboardSessionEntry(
        @NonNull String sessionId,
        @NonNull String eventClassName,
        @NonNull String state,
        int capturedEvents,
        long startedAtMillis) {}
