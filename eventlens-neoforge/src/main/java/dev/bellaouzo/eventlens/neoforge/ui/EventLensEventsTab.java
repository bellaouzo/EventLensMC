package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class EventLensEventsTab {

    private static String selected = "ClientChatEvent";
    private static String query = "";
    private static boolean confirmHot;
    private static EventList list;
    private static Button startButton;

    private EventLensEventsTab() {}

    static void init(EventLensScreen screen, EventLensUi.Frame frame) {
        EventLensUi.search(screen, frame, query, "Search events", text -> {
            query = text;
            if (list != null) {
                list.reload(screen);
            }
        });
        int listY = frame.contentY() + 22;
        int listH = frame.contentH() - 22;
        list = new EventList(screen.client(), frame.contentW(), listH, listY, 28);
        list.setX(frame.contentX());
        list.reload(screen);
        screen.add(list);
        int buttonWidth = Math.min(150, (frame.contentW() - 8) / 2);
        startButton = screen.action(
                EventLensUi.footerX(frame, 0, 2, buttonWidth),
                frame.footerY(),
                buttonWidth,
                startLabel(),
                button -> start(screen));
        screen.action(
                EventLensUi.footerX(frame, 1, 2, buttonWidth),
                frame.footerY(),
                buttonWidth,
                "Start click-flow",
                button -> startPreset(screen, "click-flow"));
    }

    private static String startLabel() {
        if (SupportedModEventTypes.isHot(selected) && !confirmHot) {
            return "Confirm hot start";
        }
        return "Start " + selected;
    }

    private static void start(EventLensScreen screen) {
        boolean hot = SupportedModEventTypes.isHot(selected);
        if (hot && !confirmHot) {
            confirmHot = true;
            if (startButton != null) {
                startButton.setMessage(Component.literal(startLabel()));
            }
            return;
        }
        startNamed(screen, selected, confirmHot || !hot);
    }

    private static void startPreset(EventLensScreen screen, String preset) {
        startNamed(screen, preset, true);
    }

    private static void startNamed(EventLensScreen screen, String query, boolean confirmed) {
        ModTraceResults.StartResult result =
                screen.coordinator().startTrace(query, playerName(), confirmed, Optional.empty());
        confirmHot = false;
        if (result.success()) {
            EventLensNotices.action(result.message());
            screen.showSession(result.sessionId());
        } else if (result.needsHotConfirm()) {
            confirmHot = true;
            if (startButton != null) {
                startButton.setMessage(Component.literal(startLabel()));
            }
        }
    }

    private static String playerName() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return "client";
        }
        return minecraft.player.getName().getString();
    }

    private static int otherMods(EventLensScreen screen, String name) {
        String className = SupportedModEventTypes.resolveClassName(name);
        List<ModHandlerRegistration> handlers =
                className == null ? List.of() : screen.coordinator().listenerRegistryPort().listHandlers(className);
        Set<String> mods = new LinkedHashSet<>();
        for (ModHandlerRegistration handler : handlers) {
            if (!"eventlens".equals(handler.modId())) {
                mods.add(handler.modId());
            }
        }
        return mods.size();
    }

    private static final class EventList extends ObjectSelectionList<EventList.Entry> {
        private EventList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private void reload(EventLensScreen screen) {
            clearEntries();
            for (String name : SupportedModEventTypes.simpleNames()) {
                if (!EventLensUi.matches(query, name, SupportedModEventTypes.summary(name))) {
                    continue;
                }
                addEntry(new Entry(name));
                if (name.equals(selected)) {
                    setSelected(children().getLast());
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
            private final String name;

            private Entry(String name) {
                this.name = name;
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
                int left = getContentX();
                int top = getContentY();
                int width = getContentWidth();
                int height = getContentHeight();
                EventLensUi.row(graphics, left, top, width, height, name.equals(selected), hovering);
                boolean hot = SupportedModEventTypes.isHot(name);
                int title = name.equals(selected) ? EventLensUi.LENS : EventLensUi.PAPER;
                graphics.text(minecraft.font, name, left + 6, top + 4, title, false);
                EventLensScreen screen = Minecraft.getInstance().gui.screen() instanceof EventLensScreen current
                        ? current
                        : null;
                int mods = screen == null ? 0 : otherMods(screen, name);
                String detail = SupportedModEventTypes.summary(name);
                if (hot) {
                    detail = "hot  ·  " + detail;
                }
                if (mods >= 2) {
                    detail = "overlap " + mods + "  ·  " + detail;
                } else if (mods == 1) {
                    detail = "1 mod  ·  " + detail;
                }
                graphics.text(minecraft.font, detail, left + 6, top + 16, EventLensUi.DIM, false);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                selected = name;
                confirmHot = false;
                setSelected(this);
                if (startButton != null) {
                    startButton.setMessage(Component.literal(startLabel()));
                }
                if (EventLensUi.doubleClicked(name) && Minecraft.getInstance().gui.screen() instanceof EventLensScreen screen) {
                    start(screen);
                }
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(name);
            }
        }
    }
}
