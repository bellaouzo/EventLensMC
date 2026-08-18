package dev.bellaouzo.eventlens.fabric.ui;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public final class EventLensHudOverlay {

    private EventLensHudOverlay() {}

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("eventlens", "hud"),
                (graphics, deltaTracker) ->
                        dev.bellaouzo.eventlens.neoforge.ui.EventLensHudOverlay.render(graphics, deltaTracker));
    }
}
