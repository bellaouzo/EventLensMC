package dev.bellaouzo.eventlens.domain.dashboard;

import org.jspecify.annotations.NonNull;

public record DashboardServerContext(
        @NonNull String paperVersion,
        @NonNull String eventLensVersion,
        @NonNull String defaultWorldName,
        @NonNull String defaultGameMode,
        int onlinePlayers,
        double tps,
        long serverTimeMillis) {}
