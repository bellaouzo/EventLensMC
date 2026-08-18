package dev.bellaouzo.eventlens.fabric.ui;

import dev.bellaouzo.eventlens.fabric.EventLensFabricMod;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EventLensScreen extends Screen {

    private String tab = "home";

    public EventLensScreen() {
        super(Component.literal("EventLens"));
    }

    public static void open() {
        net.minecraft.client.Minecraft.getInstance().setScreen(new EventLensScreen());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int x = 20;
        addRenderableWidget(Button.builder(Component.literal("Home"), button -> tab = "home")
                .bounds(x, 20, 70, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Events"), button -> tab = "events")
                .bounds(x + 76, 20, 70, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Sessions"), button -> tab = "sessions")
                .bounds(x + 152, 20, 80, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(this.width - 90, 20, 70, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        ModTraceCoordinator coordinator = EventLensFabricMod.coordinator();
        graphics.drawString(this.font, "EventLens · " + tab, 20, 48, 0xFFE8EEF5, false);
        if (coordinator == null) {
            graphics.drawString(this.font, "Coordinator not ready.", 20, 68, 0xFFFF6B6B, false);
            return;
        }
        if ("events".equals(tab)) {
            int y = 68;
            for (String event : SupportedModEventTypes.simpleNames()) {
                graphics.drawString(this.font, event, 20, y, 0xFFB8C4D4, false);
                y += 12;
                if (y > this.height - 24) {
                    break;
                }
            }
            return;
        }
        if ("sessions".equals(tab)) {
            int y = 68;
            for (var session : coordinator.listSessions()) {
                graphics.drawString(
                        this.font,
                        session.sessionId() + "  " + session.state() + "  " + session.capturedEvents(),
                        20,
                        y,
                        0xFFB8C4D4,
                        false);
                y += 12;
            }
            return;
        }
        var status = coordinator.status();
        graphics.drawString(this.font, status.platform(), 20, 68, 0xFFB8C4D4, false);
        graphics.drawString(
                this.font,
                "Tracing " + (status.tracingEnabled() ? "live" : "idle")
                        + " · "
                        + (status.agentPresent() ? "precise" : "dispatch-only"),
                20,
                84,
                0xFFB8C4D4,
                false);
    }
}
