package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.PlayerPreferencesPort;
import dev.bellaouzo.eventlens.domain.preferences.PlayerPreferences;
import dev.bellaouzo.eventlens.domain.preferences.RecentTraceEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlPlayerPreferencesStore implements PlayerPreferencesPort {

    private final JavaPlugin plugin;
    private final Map<UUID, PlayerPreferences> cache = new ConcurrentHashMap<>();

    public YamlPlayerPreferencesStore(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PlayerPreferences load(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::readFromDisk);
    }

    @Override
    public void save(UUID playerId, PlayerPreferences preferences) {
        cache.put(playerId, preferences);
        writeToDisk(playerId, preferences);
    }

    private PlayerPreferences readFromDisk(UUID playerId) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(preferencesFile(playerId));
        List<String> favorites = config.getStringList("favorites");
        List<RecentTraceEntry> recent = new ArrayList<>();
        List<Map<?, ?>> recentMaps = config.getMapList("recent-traces");
        for (Map<?, ?> entry : recentMaps) {
            Object sessionId = entry.get("session-id");
            Object event = entry.get("event");
            Object startedAt = entry.get("started-at");
            if (sessionId instanceof String session
                    && event instanceof String eventName
                    && startedAt instanceof Number at) {
                recent.add(new RecentTraceEntry(session, eventName, at.longValue()));
            }
        }
        return new PlayerPreferences(favorites, recent);
    }

    private void writeToDisk(UUID playerId, PlayerPreferences preferences) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("favorites", preferences.favoriteEvents());
        List<Map<String, Object>> recentMaps = new ArrayList<>();
        for (RecentTraceEntry entry : preferences.recentTraces()) {
            recentMaps.add(Map.of(
                    "session-id", entry.sessionId(),
                    "event", entry.eventSimpleName(),
                    "started-at", entry.startedAtMillis()));
        }
        config.set("recent-traces", recentMaps);
        try {
            config.save(preferencesFile(playerId));
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save preferences for " + playerId + ": " + ex.getMessage());
        }
    }

    private java.io.File preferencesFile(UUID playerId) {
        java.io.File directory = new java.io.File(plugin.getDataFolder(), "preferences");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new java.io.File(directory, playerId.toString().toLowerCase(Locale.ROOT) + ".yml");
    }
}
