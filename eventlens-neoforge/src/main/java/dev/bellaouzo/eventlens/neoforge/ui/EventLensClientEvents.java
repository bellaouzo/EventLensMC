package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.neoforge.EventLensNeoForgeMod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class EventLensClientEvents {

    private EventLensClientEvents() {}

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(EventLensKeybinds.OPEN);
        event.register(EventLensKeybinds.HUD);
    }

    public static void registerHud(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(EventLensNeoForgeMod.MOD_ID, "hud"),
                EventLensHudOverlay::render);
    }
}
