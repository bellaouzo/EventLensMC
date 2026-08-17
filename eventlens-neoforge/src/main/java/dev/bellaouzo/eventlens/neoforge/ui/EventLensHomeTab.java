package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import dev.bellaouzo.eventlens.modcommon.command.ModStatusHover;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

final class EventLensHomeTab {

    private EventLensHomeTab() {}

    static void init(EventLensScreen screen, EventLensUi.Frame frame) {
        EventLensUiPreferences preferences = screen.preferences();
        screen.action(
                EventLensUi.footerX(frame, 0, 2, 110),
                frame.footerY(),
                110,
                "Open Events",
                button -> screen.show(EventLensScreen.Tab.EVENTS));
        screen.action(
                EventLensUi.footerX(frame, 1, 2, 110),
                frame.footerY(),
                110,
                preferences.hudEnabled() ? "Hide HUD" : "Show HUD",
                button -> {
                    preferences.toggleHud();
                    screen.show(EventLensScreen.Tab.HOME);
                });
        int row = frame.contentY() + 118;
        ModTraceResults.Status status = screen.coordinator().status();
        int rowW = frame.contentW();
        int pauseW = 54;
        int stopW = 54;
        int viewW = Math.max(72, rowW - pauseW - stopW - 12);
        for (TraceSessionSummary session : status.sessions()) {
            if (row + 20 > frame.footerY() - 8) {
                break;
            }
            String event = SupportedModEventTypes.displaySimpleName(session.eventClassName());
            boolean paused = session.state() == TraceSessionState.PAUSED;
            boolean terminal = session.state().isTerminal();
            boolean open = session.state() == TraceSessionState.ACTIVE
                    || session.state() == TraceSessionState.THROTTLED
                    || paused;
            screen.action(
                    frame.contentX(),
                    row,
                    viewW,
                    event + "  " + EventLensUi.sessionLabel(session),
                    button -> screen.showSession(session.sessionId()));
            screen.action(
                            frame.contentX() + viewW + 6,
                            row,
                            pauseW,
                            terminal ? "Restart" : paused ? "Resume" : "Pause",
                            button -> {
                                if (terminal) {
                                    EventLensNotices.action(
                                            screen.coordinator().restartSession(session.sessionId()).message());
                                } else if (paused) {
                                    EventLensNotices.action(
                                            screen.coordinator().resumeSession(session.sessionId()).message());
                                } else {
                                    EventLensNotices.action(
                                            screen.coordinator().pauseSession(session.sessionId()).message());
                                }
                                screen.show(EventLensScreen.Tab.HOME);
                            })
                    .active = open || terminal;
            screen.action(frame.contentX() + viewW + pauseW + 12, row, stopW, "Stop", button -> {
                EventLensNotices.action(screen.coordinator().stopSession(session.sessionId()).message());
                screen.show(EventLensScreen.Tab.HOME);
            }).active = open;
            row += 24;
        }
    }

    static void render(EventLensScreen screen, GuiGraphics graphics, EventLensUi.Frame frame) {
        ModTraceResults.Status status = screen.coordinator().status();
        int cardW = (frame.contentW() - 8) / 2;
        int x = frame.contentX();
        int y = frame.contentY();
        String tracing = ModStatusHover.tracingLabel(status);
        int tracingColor =
                "live".equals(tracing) ? EventLensUi.LIVE : "paused".equals(tracing) ? EventLensUi.BRASS : EventLensUi.FAULT;
        EventLensUi.card(graphics, screen.textFont(), x, y, cardW, 42, "Platform", status.platform(), EventLensUi.PAPER, List.of());
        EventLensUi.card(
                graphics,
                screen.textFont(),
                x + cardW + 8,
                y,
                cardW,
                42,
                "Minecraft",
                status.minecraftVersion(),
                EventLensUi.PAPER,
                List.of());
        EventLensUi.card(
                graphics,
                screen.textFont(),
                x,
                y + 50,
                cardW,
                42,
                "Tracing",
                tracing,
                tracingColor,
                ModStatusHover.tracingLines(status));
        EventLensUi.card(
                graphics,
                screen.textFont(),
                x + cardW + 8,
                y + 50,
                cardW,
                42,
                "Instrumentation",
                status.agentPresent() ? "precise" : "dispatch-only",
                status.agentPresent() ? EventLensUi.LENS : EventLensUi.BRASS,
                ModStatusHover.instrumentationLines(status));
        EventLensUi.section(graphics, screen.textFont(), "Live sessions", x, y + 102);
        if (status.sessions().isEmpty()) {
            graphics.drawString(
                    screen.textFont(),
                    "None yet. Open Events to start a trace.",
                    x,
                    y + 118,
                    EventLensUi.DIM,
                    false);
        }
    }
}
