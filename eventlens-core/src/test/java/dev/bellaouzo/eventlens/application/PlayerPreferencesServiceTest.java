package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.bellaouzo.eventlens.application.port.PlayerPreferencesPort;
import dev.bellaouzo.eventlens.domain.preferences.PlayerPreferences;
import dev.bellaouzo.eventlens.domain.preferences.RecentTraceEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerPreferencesServiceTest {

    @Test
    void recordsRecentTracesWithLimit() {
        UUID playerId = UUID.randomUUID();
        MemoryPreferencesPort port = new MemoryPreferencesPort();
        PlayerPreferencesService service = new PlayerPreferencesService(
                port,
                new EventLensCommandConfig(
                        false,
                        EventLensCommandConfig.defaults().defaultDetailLevel(),
                        EventLensCommandConfig.defaults().defaultSlowThresholdNanos(),
                        true,
                        true,
                        2,
                        32,
                        Map.of(),
                        java.util.Optional.empty()));

        service.recordTraceStart(playerId, "a", "org.bukkit.event.block.BlockBreakEvent");
        service.recordTraceStart(playerId, "b", "org.bukkit.event.block.BlockPlaceEvent");
        service.recordTraceStart(playerId, "c", "org.bukkit.event.player.PlayerInteractEvent");

        List<RecentTraceEntry> recent = service.recentTraces(playerId);
        assertEquals(2, recent.size());
        assertEquals("c", recent.get(0).sessionId());
        assertEquals("b", recent.get(1).sessionId());
    }

    @Test
    void enforcesFavoriteLimit() {
        UUID playerId = UUID.randomUUID();
        MemoryPreferencesPort port = new MemoryPreferencesPort();
        PlayerPreferencesService service = new PlayerPreferencesService(
                port,
                new EventLensCommandConfig(
                        false,
                        EventLensCommandConfig.defaults().defaultDetailLevel(),
                        EventLensCommandConfig.defaults().defaultSlowThresholdNanos(),
                        true,
                        true,
                        20,
                        1,
                        Map.of(),
                        java.util.Optional.empty()));

        service.addFavorite(playerId, "BlockBreakEvent");
        assertThrows(IllegalStateException.class, () -> service.addFavorite(playerId, "BlockPlaceEvent"));
    }

    private static final class MemoryPreferencesPort implements PlayerPreferencesPort {
        private final Map<UUID, PlayerPreferences> store = new HashMap<>();

        @Override
        public PlayerPreferences load(UUID playerId) {
            return store.getOrDefault(playerId, PlayerPreferences.empty());
        }

        @Override
        public void save(UUID playerId, PlayerPreferences preferences) {
            store.put(playerId, preferences);
        }
    }
}
