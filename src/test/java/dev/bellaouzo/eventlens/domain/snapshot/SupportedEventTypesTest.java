package dev.bellaouzo.eventlens.domain.snapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SupportedEventTypesTest {

    @Test
    void listsInitialSupportedEvents() {
        assertEquals(13, SupportedEventTypes.classNames().size());
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.block.BlockBreakEvent"));
        assertTrue(SupportedEventTypes.isSupported("io.papermc.paper.event.player.AsyncChatEvent"));
        assertTrue(SupportedEventTypes.isSupported("org.bukkit.event.player.PlayerJoinEvent"));
    }

    @Test
    void simpleNamesAreSortedAndUnique() {
        assertEquals(13, SupportedEventTypes.simpleNames().size());
        assertTrue(SupportedEventTypes.simpleNames().contains("BlockBreakEvent"));
        assertTrue(SupportedEventTypes.simpleNames().contains("AsyncChatEvent"));
    }
}
