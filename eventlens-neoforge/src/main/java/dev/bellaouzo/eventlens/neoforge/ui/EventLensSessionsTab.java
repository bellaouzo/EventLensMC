package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class EventLensSessionsTab {

    private static String selected = "";
    private static String query = "";
    private static SessionList list;
    private static Button pauseButton;

    private EventLensSessionsTab() {}

    static void init(EventLensScreen screen, EventLensUi.Frame frame) {
        EventLensUi.search(screen, frame, query, "Search sessions", text -> {
            query = text;
            refresh(screen);
        });
        list = new SessionList(
                screen.client(), frame.contentW(), frame.contentH() - 22, frame.contentY() + 22, 28);
        list.setX(frame.contentX());
        refresh(screen);
        screen.add(list);
        screen.action(EventLensUi.footerX(frame, 0, 4, 80), frame.footerY(), 80, "View", button -> {
            if (!selected.isBlank()) {
                screen.showSession(selected);
            }
        });
        pauseButton = screen.action(EventLensUi.footerX(frame, 1, 4, 80), frame.footerY(), 80, "Pause", button -> {
            if (selected.isBlank()) {
                return;
            }
            String label = button.getMessage().getString();
            if ("Restart".equals(label)) {
                var result = screen.coordinator().restartSession(selected);
                EventLensNotices.action(result.message());
                if (result.success()) {
                    selected = result.sessionId();
                }
            } else if ("Resume".equals(label)) {
                EventLensNotices.action(screen.coordinator().resumeSession(selected).message());
            } else {
                EventLensNotices.action(screen.coordinator().pauseSession(selected).message());
            }
            refresh(screen);
        });
        screen.action(EventLensUi.footerX(frame, 2, 4, 80), frame.footerY(), 80, "Stop", button -> {
            if (!selected.isBlank()) {
                screen.coordinator().stopSession(selected);
                refresh(screen);
            }
        });
        screen.action(EventLensUi.footerX(frame, 3, 4, 80), frame.footerY(), 80, "Export", button -> export(screen));
    }

    static void refresh(EventLensScreen screen) {
        if (list == null) {
            return;
        }
        list.reload(screen);
        updatePauseButton(screen);
    }

    private static void updatePauseButton(EventLensScreen screen) {
        if (pauseButton == null) {
            return;
        }
        TraceSessionSummary session = selectedSession(screen);
        boolean paused = session != null && session.state() == TraceSessionState.PAUSED;
        boolean terminal = session != null && session.state().isTerminal();
        boolean open = session != null
                && (session.state() == TraceSessionState.ACTIVE
                        || session.state() == TraceSessionState.THROTTLED
                        || paused);
        pauseButton.setMessage(Component.literal(terminal ? "Restart" : paused ? "Resume" : "Pause"));
        pauseButton.active = open || terminal;
    }

    private static TraceSessionSummary selectedSession(EventLensScreen screen) {
        if (selected.isBlank()) {
            return null;
        }
        for (TraceSessionSummary session : screen.coordinator().listSessions()) {
            if (session.sessionId().equals(selected)) {
                return session;
            }
        }
        return null;
    }

    private static void export(EventLensScreen screen) {
        if (selected.isBlank()) {
            return;
        }
        EventLensNotices.export(screen.coordinator().exportSession(selected, Optional.empty()));
    }

    private static final class SessionList extends ObjectSelectionList<SessionList.Entry> {
        private SessionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private void reload(EventLensScreen screen) {
            clearEntries();
            List<TraceSessionSummary> sessions = screen.coordinator().listSessions();
            if (selected.isBlank() && !sessions.isEmpty()) {
                selected = sessions.getFirst().sessionId();
            }
            for (TraceSessionSummary session : sessions) {
                String event = SupportedModEventTypes.displaySimpleName(session.eventClassName());
                if (!EventLensUi.matches(
                        query,
                        session.sessionId(),
                        event,
                        String.valueOf(session.state()),
                        session.restartBadge())) {
                    continue;
                }
                Entry entry = new Entry(session);
                addEntry(entry);
                if (session.sessionId().equals(selected)) {
                    setSelected(entry);
                }
            }
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {}

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {}

        @Override
        public int getRowWidth() {
            return width - 8;
        }

        private final class Entry extends ObjectSelectionList.Entry<Entry> {
            private final TraceSessionSummary session;

            private Entry(TraceSessionSummary session) {
                this.session = session;
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics, int index, int top, boolean hovering, float partialTick) {
                int left = getContentX();
                int width = getContentWidth();
                int height = getContentHeight();
                boolean on = session.sessionId().equals(selected);
                EventLensUi.row(graphics, left, top, width, height, on, hovering);
                String event = SupportedModEventTypes.displaySimpleName(session.eventClassName());
                graphics.text(
                        minecraft.font,
                        EventLensUi.sessionLabel(session),
                        left + 6,
                        top + 4,
                        session.restarted() ? EventLensUi.BRASS : on ? EventLensUi.LENS : EventLensUi.PAPER,
                        false);
                int stateColor = session.state() == TraceSessionState.ACTIVE ? EventLensUi.LIVE : EventLensUi.BRASS;
                String stateLine = (session.restarted() ? session.restartBadge() + "  ·  " : "")
                        + session.state()
                        + "  ·  "
                        + event
                        + "  ·  "
                        + session.capturedEvents()
                        + " captured";
                graphics.text(minecraft.font, stateLine, left + 6, top + 16, stateColor, false);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                selected = session.sessionId();
                setSelected(this);
                if (Minecraft.getInstance().gui.screen() instanceof EventLensScreen screen) {
                    updatePauseButton(screen);
                    if (EventLensUi.doubleClicked(session.sessionId())) {
                        screen.showSession(session.sessionId());
                    }
                }
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(session.sessionId());
            }
        }
    }
}
