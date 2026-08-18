package dev.bellaouzo.eventlens.domain.snapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SupportedEventTypesTest {

    @Test
    void listsInitialSupportedEvents() {
        assertEquals(141, SupportedEventTypes.classNames().size());
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.block.BlockBreakEvent"));
        assertTrue(SupportedEventTypes.isSupported("io.papermc.paper.event.player.AsyncChatEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.player.PlayerJoinEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.inventory.InventoryOpenEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.server.ServerCommandEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.entity.EntityExplodeEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.entity.EntityDamageByEntityEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.player.PlayerInteractEntityEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.player.PlayerFishEvent"));
    }

    @Test
    void simpleNamesAreSortedAndUnique() {
        assertEquals(141, SupportedEventTypes.simpleNames().size());
        assertTrue(SupportedEventTypes.simpleNames().contains("BlockBreakEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("AsyncChatEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("EntityExplodeEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("PlayerKickEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("PlayerGameModeChangeEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("EntityBreedEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("ChunkLoadEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("PrepareAnvilEvent"));
    }
}
