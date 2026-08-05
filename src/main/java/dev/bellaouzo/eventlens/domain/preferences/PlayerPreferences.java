package dev.bellaouzo.eventlens.domain.preferences;

import java.util.List;
import java.util.Objects;

public record PlayerPreferences(List<String> favoriteEvents, List<RecentTraceEntry> recentTraces) {

    public PlayerPreferences {
        favoriteEvents = favoriteEvents == null ? List.of() : List.copyOf(favoriteEvents);
        recentTraces = recentTraces == null ? List.of() : List.copyOf(recentTraces);
    }

    public static PlayerPreferences empty() {
        return new PlayerPreferences(List.of(), List.of());
    }

    public PlayerPreferences withFavoriteEvents(List<String> events) {
        return new PlayerPreferences(List.copyOf(Objects.requireNonNull(events)), recentTraces);
    }

    public PlayerPreferences withRecentTraces(List<RecentTraceEntry> traces) {
        return new PlayerPreferences(favoriteEvents, List.copyOf(Objects.requireNonNull(traces)));
    }
}
