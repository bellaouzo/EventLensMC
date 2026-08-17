package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
        list = new LineList(screen.getMinecraft(), frame.contentW(), frame.contentH() - 22, frame.contentY() + 22, 18);
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

    private static final class LineList extends ObjectSelectionList<LineList.Entry> {
        private LineList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private void reload(EventLensScreen screen) {
            clearEntries();
            setSelected(null);
            for (EventLensSessionRows.Row row : EventLensSessionRows.build(screen)) {
                if (EventLensUi.matches(query, row.primary(), row.secondary())) {
                    addEntry(new Entry(row));
                }
            }
        }

        @Override
        protected void renderListBackground(GuiGraphics graphics) {}

        @Override
        protected void renderListSeparators(GuiGraphics graphics) {}

        @Override
        public int getRowWidth() {
            return width;
        }

        @Override
        public int getRowLeft() {
            return getX();
        }

        @Override
        protected void renderSelection(GuiGraphics graphics, int top, int width, int height, int outer, int inner) {}

        private final class Entry extends ObjectSelectionList.Entry<Entry> {
            private final EventLensSessionRows.Row row;

            private Entry(EventLensSessionRows.Row row) {
                this.row = row;
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
                boolean dispatch = row.kind() == EventLensSessionRows.Kind.DISPATCH;
                EventLensUi.rowSlot(graphics, getX(), LineList.this.width, top, height, false, hovering && dispatch);
                paint(graphics, minecraft.font, left, top, width, height);
            }

            private void paint(GuiGraphics graphics, Font font, int left, int top, int width, int height) {
                int x = left + 6;
                int y = top + 3;
                switch (row.kind()) {
                    case TITLE -> {
                        graphics.drawString(font, row.primary(), x, y, EventLensUi.LENS, false);
                        int timeX = left + width - 8 - font.width(row.secondary());
                        graphics.drawString(
                                font,
                                row.cancelled() ? row.secondary() + "  cancelled" : row.secondary(),
                                timeX,
                                y,
                                row.cancelled() ? EventLensUi.FAULT : EventLensUi.BRASS,
                                false);
                    }
                    case SECTION -> {
                        graphics.drawString(font, row.primary(), x, y, EventLensUi.BRASS, false);
                        graphics.fill(x, top + height - 1, left + width - 6, top + height, EventLensUi.STEEL);
                    }
                    case FIELD -> {
                        graphics.drawString(font, row.primary(), x, y, EventLensUi.DIM, false);
                        graphics.drawString(
                                font, row.secondary(), x + Math.max(64, font.width(row.primary()) + 10), y, EventLensUi.PAPER, false);
                    }
                    case HANDLER -> {
                        int color = row.cancelled() ? EventLensUi.FAULT : EventLensUi.PAPER;
                        graphics.drawString(font, row.primary(), x, y, color, false);
                        graphics.drawString(
                                font,
                                row.cancelled() ? row.secondary() + "  cancelled" : row.secondary(),
                                x + Math.max(72, font.width(row.primary()) + 10),
                                y,
                                EventLensUi.DIM,
                                false);
                    }
                    case DISPATCH -> graphics.drawString(
                            font, row.primary(), x, y, row.cancelled() ? EventLensUi.FAULT : EventLensUi.PAPER, false);
                    case NOTE -> graphics.drawString(font, row.primary(), x, y, EventLensUi.DIM, false);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (row.kind() == EventLensSessionRows.Kind.DISPATCH
                        && row.sequence() >= 0
                        && Minecraft.getInstance().screen instanceof EventLensScreen screen) {
                    screen.showDispatch(row.sequence());
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(row.primary() + " " + row.secondary());
            }
        }
    }
}
