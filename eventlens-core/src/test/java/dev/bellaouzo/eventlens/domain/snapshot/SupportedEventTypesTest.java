package dev.bellaouzo.eventlens.domain.snapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SupportedEventTypesTest {

    @Test
    void listsInitialSupportedEvents() {
        assertEquals(22, SupportedEventTypes.classNames().size());
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.block.BlockBreakEvent"));
        assertTrue(SupportedEventTypes.isSupported("io.papermc.paper.event.player.AsyncChatEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.player.PlayerJoinEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.inventory.InventoryOpenEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.server.ServerCommandEvent"));
    }

    @Test
    void simpleNamesAreSortedAndUnique() {
        assertEquals(22, SupportedEventTypes.simpleNames().size());
        assertTrue(SupportedEventTypes.simpleNames().contains("BlockBreakEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("AsyncChatEvent"));
    }
}
