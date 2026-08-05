package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.PlayerPreferencesPort;
import dev.bellaouzo.eventlens.domain.preferences.PlayerPreferences;
import dev.bellaouzo.eventlens.domain.preferences.RecentTraceEntry;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerPreferencesService {

    private final PlayerPreferencesPort port;
    private final EventLensCommandConfig commandConfig;

    public PlayerPreferencesService(PlayerPreferencesPort port, EventLensCommandConfig commandConfig) {
        this.port = port;
        this.commandConfig = commandConfig;
    }

    public List<String> favorites(UUID playerId) {
        return port.load(playerId).favoriteEvents();
    }

    public List<RecentTraceEntry> recentTraces(UUID playerId) {
        return port.load(playerId).recentTraces();
    }

    public boolean addFavorite(UUID playerId, String eventQuery) {
        String normalized = normalizeEvent(eventQuery);
        if (normalized == null) {
            return false;
        }
        PlayerPreferences current = port.load(playerId);
        List<String> favorites = new ArrayList<>(current.favoriteEvents());
        if (favorites.stream().anyMatch(existing -> existing.equalsIgnoreCase(normalized))) {
            return true;
        }
        if (favorites.size() >= commandConfig.maxFavorites()) {
            throw new IllegalStateException(
                    "Favorite limit reached (" + commandConfig.maxFavorites() + "). Remove one first.");
        }
        favorites.add(normalized);
        port.save(playerId, current.withFavoriteEvents(favorites));
        return true;
    }

    public boolean removeFavorite(UUID playerId, String eventQuery) {
        String normalized = normalizeEvent(eventQuery);
        if (normalized == null) {
            return false;
        }
        PlayerPreferences current = port.load(playerId);
        List<String> favorites = new ArrayList<>(current.favoriteEvents());
        boolean removed = favorites.removeIf(existing -> existing.equalsIgnoreCase(normalized));
        if (removed) {
            port.save(playerId, current.withFavoriteEvents(favorites));
        }
        return removed;
    }

    public void recordTraceStart(UUID playerId, String sessionId, String eventClassName) {
        PlayerPreferences current = port.load(playerId);
        List<RecentTraceEntry> recent = new ArrayList<>(current.recentTraces());
        recent.removeIf(entry -> entry.sessionId().equals(sessionId));
        recent.add(
                0,
                new RecentTraceEntry(
                        sessionId, SupportedEventTypes.displaySimpleName(eventClassName), System.currentTimeMillis()));
        while (recent.size() > commandConfig.maxRecentTraces()) {
            recent.remove(recent.size() - 1);
        }
        port.save(playerId, current.withRecentTraces(recent));
    }

    private static String normalizeEvent(String eventQuery) {
        if (eventQuery == null || eventQuery.isBlank()) {
            return null;
        }
        return eventQuery.trim();
    }
}
