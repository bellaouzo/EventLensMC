package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.neoforge.EventLensNeoForgeMod;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class EventLensClientEvents {

    private EventLensClientEvents() {}

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(EventLensKeybinds.CATEGORY);
        event.register(EventLensKeybinds.OPEN);
        event.register(EventLensKeybinds.HUD);
    }

    public static void registerHud(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(EventLensNeoForgeMod.MOD_ID, "hud"),
                EventLensHudOverlay::render);
    }
}
