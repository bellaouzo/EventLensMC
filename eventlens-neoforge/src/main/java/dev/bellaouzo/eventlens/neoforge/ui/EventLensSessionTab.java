package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

final class EventLensSessionTab {

    private static String query = "";
    private static LineList list;

    private EventLensSessionTab() {}

    static void init(EventLensScreen screen, EventLensUi.Frame frame) {
        EventLensUi.search(screen, frame, query, EventLensSessionRuns.searchHint(screen), text -> {
            query = text;
            refresh(screen);
        });
        boolean history = !EventLensSessionRuns.label(screen).isBlank();
        int actions = history ? 5 : 4;
        int buttonW = history ? 64 : 80;
        screen.action(EventLensUi.footerX(frame, 0, actions, buttonW), frame.footerY(), buttonW, "Back", button -> {
            if (screen.dispatchSequence() >= 0) {
                screen.showSession(screen.sessionId());
            } else {
                screen.show(EventLensScreen.Tab.SESSIONS);
            }
        });
        var summary = screen.coordinator().sessionManager().getSessionDetail(screen.sessionId())
                .map(TraceSessionDetail::summary)
                .orElse(null);
        screen.action(
                        EventLensUi.footerX(frame, 1, actions, buttonW),
                        frame.footerY(),
                        buttonW,
                        EventLensSessionActions.captureLabel(summary),
                        button -> EventLensSessionActions.applyCapture(
                                screen, screen.sessionId(), button.getMessage().getString()))
                .active = EventLensSessionActions.captureActive(summary);
        screen.action(EventLensUi.footerX(frame, 2, actions, buttonW), frame.footerY(), buttonW, "Stop", button -> {
            var stopped = screen.coordinator().stopSession(screen.sessionId());
            EventLensNotices.action(stopped.message());
            screen.show(EventLensScreen.Tab.SESSIONS);
        }).active = EventLensSessionActions.isOpen(summary);
        screen.action(EventLensUi.footerX(frame, 3, actions, buttonW), frame.footerY(), buttonW, "Export", button -> {
            EventLensNotices.export(screen.coordinator().exportSession(screen.sessionId(), screen.sessionGenerationOption()));
        });
        if (history) {
            screen.action(
                    EventLensUi.footerX(frame, 4, actions, buttonW),
                    frame.footerY(),
                    buttonW,
                    EventLensSessionRuns.label(screen),
                    button -> EventLensSessionRuns.cycle(screen));
        }
        list = new LineList(screen.getMinecraft(), frame.contentW(), frame.contentH() - 22, frame.contentY() + 22, 14);
        list.setX(frame.contentX());
        refresh(screen);
        screen.add(list);
    }

    static void refresh(EventLensScreen screen) {
        if (list == null) {
            return;
        }
        list.reload(screen);
    }

    static void render(EventLensScreen screen, GuiGraphics graphics, EventLensUi.Frame frame) {}

    private static List<String> lines(EventLensScreen screen) {
        if (screen.dispatchSequence() >= 0) {
            ModTraceResults.ViewResult result = screen.coordinator()
                    .viewDispatch(screen.sessionId(), screen.dispatchSequence(), screen.sessionGenerationOption());
            if (result.records().isEmpty()) {
                return List.of(result.message());
            }
            return detailLines(result.records().getFirst());
        }
        Optional<TraceSessionDetail> detail = screen.coordinator()
                .sessionManager()
                .getSessionDetail(screen.sessionId(), screen.sessionGenerationOption());
        if (detail.isEmpty()) {
            return List.of("Session not found: " + screen.sessionId());
        }
        List<TraceDispatchRecord> records = detail.orElseThrow().records();
        if (records.isEmpty()) {
            return List.of("Nothing captured yet. Trigger the event.");
        }
        int from = Math.max(0, records.size() - 64);
        List<String> lines = new ArrayList<>();
        if (from > 0) {
            lines.add("Showing last 64 of " + records.size());
        }
        for (int i = from; i < records.size(); i++) {
            TraceDispatchRecord record = records.get(i);
            lines.add(String.format(Locale.ROOT, "#%d  %.2f ms", record.sequence(), record.durationNanos() / 1_000_000.0));
        }
        return lines;
    }

    private static List<String> detailLines(TraceDispatchRecord record) {
        List<String> lines = new ArrayList<>();
        lines.add(String.format(Locale.ROOT, "Dispatch #%d  %.2f ms", record.sequence(), record.durationNanos() / 1_000_000.0));
        List<SnapshotField> fields = record.snapshotAfter().fields();
        if (fields == null || fields.isEmpty()) {
            lines.add("  no fields");
        } else {
            int shown = 0;
            for (SnapshotField field : fields) {
                if (shown++ >= 16) {
                    lines.add("  …");
                    break;
                }
                lines.add("  " + field.name() + "  " + displayValue(field.value()));
            }
        }
        List<ListenerTimingRecord> timings = record.listenerTimings();
        if (timings != null) {
            for (ListenerTimingRecord timing : timings) {
                String cancel = timing.cancellationTransition()
                        .filter(transition -> transition.kind() == CancellationTransitionKind.BECAME_CANCELLED)
                        .map(ignored -> "  cancelled")
                        .orElse("");
                lines.add(String.format(
                        Locale.ROOT,
                        "  #%d  %s  %s#%s  %.2f ms%s",
                        timing.invocationOrder(),
                        timing.pluginName(),
                        simpleName(timing.listenerClassName()),
                        timing.methodName(),
                        timing.durationNanos() / 1_000_000.0,
                        cancel));
            }
        }
        return lines;
    }

    private static String displayValue(SnapshotValue value) {
        return switch (value) {
            case SnapshotValue.Present present -> present.display();
            case SnapshotValue.Truncated truncated -> truncated.display();
            case SnapshotValue.Unsupported unsupported -> "?" + unsupported.reason();
        };
    }

    private static String simpleName(String className) {
        int dot = className.lastIndexOf('.');
        return dot < 0 ? className : className.substring(dot + 1);
    }

    private static final class LineList extends ObjectSelectionList<LineList.Entry> {
        private LineList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private void reload(EventLensScreen screen) {
            clearEntries();
            for (String line : lines(screen)) {
                if (EventLensUi.matches(query, line)) {
                    addEntry(new Entry(line));
                }
            }
        }

        @Override
        protected void renderListBackground(GuiGraphics graphics) {}

        @Override
        protected void renderListSeparators(GuiGraphics graphics) {}

        @Override
        public int getRowWidth() {
            return width - 8;
        }

        private final class Entry extends ObjectSelectionList.Entry<Entry> {
            private final String text;

            private Entry(String text) {
                this.text = text;
            }

            @Override
            public void render(
                    GuiGraphics graphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovering,
                    float partialTick) {
                boolean clickable = text.startsWith("#");
                EventLensUi.row(graphics, left, top, width, height, false, hovering && clickable);
                int color = clickable ? EventLensUi.PAPER : EventLensUi.DIM;
                if (text.contains("cancelled")) {
                    color = EventLensUi.FAULT;
                }
                graphics.drawString(minecraft.font, text, left + 6, top + 3, color, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (text.startsWith("#") && Minecraft.getInstance().screen instanceof EventLensScreen screen) {
                    int space = text.indexOf(' ');
                    String token = space < 0 ? text.substring(1) : text.substring(1, space);
                    try {
                        screen.showDispatch(Integer.parseInt(token));
                    } catch (NumberFormatException ignored) {
                        return false;
                    }
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(text);
            }
        }
    }
}
