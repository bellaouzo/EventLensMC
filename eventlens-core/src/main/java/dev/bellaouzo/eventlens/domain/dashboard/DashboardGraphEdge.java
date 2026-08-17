package dev.bellaouzo.eventlens.domain.dashboard;

import org.jspecify.annotations.NonNull;

public record DashboardGraphEdge(
        @NonNull String sourceId, @NonNull String targetId, int weight, @NonNull String label) {}
