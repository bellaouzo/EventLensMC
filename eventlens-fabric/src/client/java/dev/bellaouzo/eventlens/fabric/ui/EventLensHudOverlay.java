package dev.bellaouzo.eventlens.fabric.ui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class EventLensHudOverlay {

    private EventLensHudOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(
                (graphics, tickCounter) ->
                        dev.bellaouzo.eventlens.neoforge.ui.EventLensHudOverlay.render(graphics, null));
    }
}
