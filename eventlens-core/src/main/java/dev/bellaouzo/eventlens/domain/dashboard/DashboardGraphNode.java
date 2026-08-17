package dev.bellaouzo.eventlens.domain.dashboard;

import org.jspecify.annotations.NonNull;

public record DashboardGraphNode(@NonNull String id, @NonNull String label, @NonNull Kind kind, int weight) {

    public enum Kind {
        EVENT,
        PLUGIN
    }
}
