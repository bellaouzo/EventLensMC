package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class EventLensHudOverlay {

    private static final long REFRESH_NANOS = 250_000_000L;
    private static long lastRefreshNanos;
    private static String line1 = "";
    private static String line2 = "";

    private EventLensHudOverlay() {}

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        EventLensUiPreferences preferences = EventLensClientAccess.preferences();
        if (preferences == null || !preferences.hudEnabled() || EventLensScreen.isOpen()) {
            return;
        }
        ModTraceCoordinator coordinator = EventLensClientAccess.coordinator();
        if (coordinator == null || !coordinator.sessionManager().isTracingEnabled()) {
            return;
        }
        long now = System.nanoTime();
        if (now - lastRefreshNanos >= REFRESH_NANOS) {
            lastRefreshNanos = now;
            refresh(coordinator);
        }
        if (line1.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int width = Math.max(font.width(line1), font.width(line2)) + 18;
        int x = 6;
        int y = 6;
        graphics.fill(x + 2, y + 2, x + width + 2, y + 32, 0x66000000);
        graphics.fill(x, y, x + width, y + 30, EventLensUi.INK);
        graphics.renderOutline(x, y, width, 30, EventLensUi.STEEL);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, EventLensUi.HIGHLIGHT);
        graphics.drawString(font, line1, x + 8, y + 5, EventLensUi.PAPER, false);
        graphics.drawString(font, line2, x + 8, y + 16, EventLensUi.BRASS, false);
    }

    private static void refresh(ModTraceCoordinator coordinator) {
        List<TraceSessionSummary> sessions = coordinator.listSessions();
        TraceSessionSummary active = null;
        TraceSessionSummary paused = null;
        for (TraceSessionSummary session : sessions) {
            if (session.state() == TraceSessionState.ACTIVE || session.state() == TraceSessionState.THROTTLED) {
                active = session;
                break;
            }
            if (paused == null && session.state() == TraceSessionState.PAUSED) {
                paused = session;
            }
        }
        if (active == null && paused != null) {
            line1 = SupportedModEventTypes.displaySimpleName(paused.eventClassName());
            line2 = "paused  ·  " + paused.sessionId();
            return;
        }
        if (active == null) {
            line1 = "";
            line2 = "";
            return;
        }
        String event = SupportedModEventTypes.displaySimpleName(active.eventClassName());
        Optional<TraceSessionDetail> detail = coordinator.sessionManager().getSessionDetail(active.sessionId());
        if (detail.isEmpty() || detail.orElseThrow().records().isEmpty()) {
            line1 = event;
            line2 = "waiting  ·  " + active.sessionId();
            return;
        }
        TraceDispatchRecord last = detail.orElseThrow().records().getLast();
        line1 = String.format(Locale.ROOT, "%s  ·  #%d", event, last.sequence());
        line2 = String.format(Locale.ROOT, "%.2f ms  ·  %s", last.durationNanos() / 1_000_000.0, handlers(last));
    }

    private static String handlers(TraceDispatchRecord record) {
        List<ListenerTimingRecord> timings = record.listenerTimings();
        if (timings == null || timings.isEmpty()) {
            return "dispatch-only";
        }
        ListenerTimingRecord slowest = timings.getFirst();
        for (ListenerTimingRecord timing : timings) {
            if (timing.durationNanos() > slowest.durationNanos()) {
                slowest = timing;
            }
        }
        return timings.size() + " handlers  ·  " + slowest.pluginName();
    }
}
