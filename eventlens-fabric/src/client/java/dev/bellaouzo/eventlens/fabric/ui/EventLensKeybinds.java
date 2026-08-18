package dev.bellaouzo.eventlens.fabric.ui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensClientAccess;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensScreen;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensUiPreferences;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class EventLensKeybinds {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("eventlens", "key.categories.eventlens"));

    private static final KeyMapping OPEN = new KeyMapping(
            "key.eventlens.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    private static final KeyMapping HUD = new KeyMapping(
            "key.eventlens.hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);

    private EventLensKeybinds() {}

    public static void register() {
        KeyMappingHelper.registerKeyMapping(OPEN);
        KeyMappingHelper.registerKeyMapping(HUD);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN.consumeClick()) {
                if (EventLensScreen.isOpen()) {
                    client.gui.setScreen(null);
                } else {
                    EventLensScreen.open();
                }
            }
            while (HUD.consumeClick()) {
                EventLensUiPreferences preferences = EventLensClientAccess.preferences();
                if (preferences != null) {
                    preferences.toggleHud();
                }
            }
        });
    }
}
