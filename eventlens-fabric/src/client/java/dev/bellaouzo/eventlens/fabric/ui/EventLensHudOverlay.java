package dev.bellaouzo.eventlens.fabric.ui;

import dev.bellaouzo.eventlens.fabric.EventLensFabricMod;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class EventLensHudOverlay {

    private static boolean enabled;

    private EventLensHudOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> render(graphics));
    }

    public static void toggle() {
        enabled = !enabled;
    }

    private static void render(GuiGraphics graphics) {
        if (!enabled || Minecraft.getInstance().screen instanceof EventLensScreen) {
            return;
        }
        var coordinator = EventLensFabricMod.coordinator();
        if (coordinator == null) {
            return;
        }
        var active = coordinator.listSessions().stream()
                .filter(session -> "ACTIVE".equals(session.state().name()) || "THROTTLED".equals(session.state().name()))
                .findFirst();
        if (active.isEmpty()) {
            return;
        }
        String event = SupportedModEventTypes.displaySimpleName(active.get().eventClassName());
        graphics.drawString(
                Minecraft.getInstance().font,
                "EL " + event + " #" + active.get().capturedEvents(),
                8,
                8,
                0xFFE8EEF5,
                true);
    }
}
