package dev.bellaouzo.eventlens.modcommon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SupportedModEventTypesTest {

    @Test
    void resolvesKnownEventsAndMarksHotOnes() {
        assertTrue(SupportedModEventTypes.simpleNames().contains("ClientUseItemEvent"));
        assertTrue(SupportedModEventTypes.simpleNames().contains("ClientChatEvent"));
        assertTrue(SupportedModEventTypes.simpleNames().contains("ClientJoinEvent"));
        assertTrue(SupportedModEventTypes.simpleNames().contains("ClientUseEntityEvent"));
        assertTrue(SupportedModEventTypes.simpleNames().contains("ClientKeyEvent"));
        assertTrue(SupportedModEventTypes.simpleNames().contains("ClientRespawnEvent"));
        assertTrue(SupportedModEventTypes.simpleNames().contains("ClientEntityJoinEvent"));
        assertTrue(SupportedModEventTypes.simpleNames().contains("ClientSoundEvent"));
        assertEquals(33, SupportedModEventTypes.simpleNames().size());
        assertEquals(
                SupportedModEventTypes.CLIENT_USE_ITEM_EVENT,
                SupportedModEventTypes.resolveClassName("clientuseitemevent"));
        assertTrue(SupportedModEventTypes.isHot("ClientTickEvent"));
        assertTrue(SupportedModEventTypes.isHot(SupportedModEventTypes.CLIENT_PLAYER_MOVE_EVENT));
        assertTrue(SupportedModEventTypes.isHot("ClientTooltipEvent"));
        assertTrue(SupportedModEventTypes.isHot("ClientChunkLoadEvent"));
        assertEquals("Chat you send", SupportedModEventTypes.summary("ClientChatEvent"));
        assertFalse(SupportedModEventTypes.isHot("ClientChatEvent"));
        assertFalse(SupportedModEventTypes.isHot("ClientKeyEvent"));
        assertFalse(SupportedModEventTypes.isSupportedSimpleName("BlockBreakEvent"));
        assertFalse(SupportedModEventTypes.isSupportedSimpleName("RenderGuiEvent"));
    }
}
