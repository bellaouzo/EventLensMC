package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
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
        list = new EventList(screen.getMinecraft(), frame.contentW(), listH, listY, 28);
        list.setX(frame.contentX());
        list.reload(screen);
        screen.add(list);
        startButton = screen.action(
                EventLensUi.footerX(frame, 0, 1, Math.min(240, frame.contentW())),
                frame.footerY(),
                Math.min(240, frame.contentW()),
                startLabel(),
                button -> start(screen));
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
        ModTraceResults.StartResult result =
                screen.coordinator().startTrace(selected, playerName(), confirmHot || !hot, Optional.empty());
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
        return minecraft.player.getGameProfile().getName();
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
        protected void renderListBackground(GuiGraphics graphics) {}

        @Override
        protected void renderListSeparators(GuiGraphics graphics) {}

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
                EventLensUi.row(graphics, left, top, width, height, name.equals(selected), hovering);
                boolean hot = SupportedModEventTypes.isHot(name);
                int title = name.equals(selected) ? EventLensUi.LENS : EventLensUi.PAPER;
                graphics.drawString(minecraft.font, name, left + 6, top + 4, title, false);
                EventLensScreen screen = Minecraft.getInstance().screen instanceof EventLensScreen current
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
                graphics.drawString(minecraft.font, detail, left + 6, top + 16, EventLensUi.DIM, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                selected = name;
                confirmHot = false;
                setSelected(this);
                if (startButton != null) {
                    startButton.setMessage(Component.literal(startLabel()));
                }
                if (EventLensUi.doubleClicked(name) && Minecraft.getInstance().screen instanceof EventLensScreen screen) {
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
