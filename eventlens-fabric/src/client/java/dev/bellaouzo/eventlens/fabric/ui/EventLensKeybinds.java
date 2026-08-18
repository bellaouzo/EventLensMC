package dev.bellaouzo.eventlens.fabric.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class EventLensKeybinds {

    private static final KeyMapping OPEN = new KeyMapping(
            "key.eventlens.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.eventlens");
    private static final KeyMapping HUD = new KeyMapping(
            "key.eventlens.hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.eventlens");

    private EventLensKeybinds() {}

    public static void register() {
        KeyBindingHelper.registerKeyBinding(OPEN);
        KeyBindingHelper.registerKeyBinding(HUD);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN.consumeClick()) {
                if (client.screen instanceof EventLensScreen) {
                    client.setScreen(null);
                } else {
                    EventLensScreen.open();
                }
            }
            while (HUD.consumeClick()) {
                EventLensHudOverlay.toggle();
            }
        });
    }
}
