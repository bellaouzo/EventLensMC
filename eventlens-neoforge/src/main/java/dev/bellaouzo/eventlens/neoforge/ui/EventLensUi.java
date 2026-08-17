package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.command.ModStatusHover;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

final class EventLensUi {

    static final int INK = 0xE028282E;
    static final int WELL = 0xD01E1E24;
    static final int STEEL = 0xFF4A4A54;
    static final int HIGHLIGHT = 0xFF5A5A66;
    static final int SHADOW = 0xFF141418;
    static final int LENS = 0xFF5EC8E8;
    static final int BRASS = 0xFFE8C36A;
    static final int PAPER = 0xFFE8E4DC;
    static final int DIM = 0xFF8A8680;
    static final int LIVE = 0xFF6FCF7A;
    static final int FAULT = 0xFFE06A6A;
    static final int ROW = 0x335EC8E8;
    static final int HOVER = 0x22FFFFFF;
    static final int DIM_WORLD = 0x58101820;
    static final int PILL_LIVE = 0xCC1E3A28;
    static final int PILL_OFF = 0xCC3A1E1E;
    static final int PILL_WARN = 0xCC3A3218;
    static final int PILL_LENS = 0xCC163038;

    private static final List<Hit> hits = new ArrayList<>();

    private EventLensUi() {}

    record Hit(int x, int y, int width, int height, List<Component> lines) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    record Frame(
            int x,
            int y,
            int width,
            int height,
            int tabY,
            int contentX,
            int contentY,
            int contentW,
            int contentH,
            int footerY) {}

    static Frame frame(int screenW, int screenH) {
        int width = Math.min(440, screenW - 28);
        int height = Math.min(268, screenH - 20);
        int x = (screenW - width) / 2;
        int y = (screenH - height) / 2;
        int contentX = x + 12;
        int contentY = y + 54;
        int footerY = y + height - 30;
        return new Frame(
                x,
                y,
                width,
                height,
                y + 28,
                contentX,
                contentY,
                width - 24,
                Math.max(48, footerY - contentY - 8),
                footerY);
    }

    static void dimWorld(GuiGraphics graphics, int screenW, int screenH) {
        graphics.fill(0, 0, screenW, screenH, DIM_WORLD);
    }

    static void panel(GuiGraphics graphics, Frame frame) {
        graphics.fill(frame.x + 3, frame.y + 3, frame.x + frame.width + 3, frame.y + frame.height + 3, 0x66000000);
        graphics.fill(frame.x, frame.y, frame.x + frame.width, frame.y + frame.height, INK);
        graphics.renderOutline(frame.x, frame.y, frame.width, frame.height, STEEL);
        graphics.fill(frame.x + 1, frame.y + 1, frame.x + frame.width - 1, frame.y + 2, HIGHLIGHT);
        graphics.fill(frame.x + 1, frame.y + frame.height - 2, frame.x + frame.width - 1, frame.y + frame.height - 1, SHADOW);
        graphics.fill(frame.x + 1, frame.y + 24, frame.x + frame.width - 1, frame.y + 25, STEEL);
        graphics.fill(frame.x + 1, frame.footerY - 6, frame.x + frame.width - 1, frame.footerY - 5, STEEL);
    }

    static void well(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, WELL);
        graphics.fill(x, y, x + width, y + 1, SHADOW);
        graphics.fill(x, y, x + 1, y + height, SHADOW);
        graphics.fill(x + 1, y + height - 1, x + width, y + height, HIGHLIGHT);
        graphics.fill(x + width - 1, y, x + width, y + height, HIGHLIGHT);
    }

    static void beginHits() {
        hits.clear();
    }

    static void addHit(int x, int y, int width, int height, List<String> lines) {
        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            components.add(Component.literal(line));
        }
        hits.add(new Hit(x, y, width, height, components));
    }

    static void renderHover(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        for (Hit hit : hits) {
            if (hit.contains(mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, hit.lines(), mouseX, mouseY);
                return;
            }
        }
    }

    static void header(GuiGraphics graphics, Font font, Frame frame, ModTraceResults.Status status) {
        graphics.drawString(font, "EventLens", frame.x + 12, frame.y + 8, PAPER, false);
        int pillX = frame.x + frame.width - 12;
        String agentLabel = status.agentPresent() ? "precise" : "dispatch";
        int agentW = pill(
                graphics,
                font,
                pillX,
                frame.y + 6,
                agentLabel,
                status.agentPresent() ? PILL_LENS : PILL_WARN,
                status.agentPresent() ? LENS : BRASS,
                true);
        addHit(pillX - agentW, frame.y + 6, agentW, 13, ModStatusHover.instrumentationLines(status));
        pillX -= agentW + 4;
        String tracingLabel = ModStatusHover.tracingLabel(status);
        int fill = "live".equals(tracingLabel) ? PILL_LIVE : "paused".equals(tracingLabel) ? PILL_WARN : PILL_OFF;
        int ink = "live".equals(tracingLabel) ? LIVE : "paused".equals(tracingLabel) ? BRASS : FAULT;
        int tracingW = pill(graphics, font, pillX, frame.y + 6, tracingLabel, fill, ink, true);
        addHit(pillX - tracingW, frame.y + 6, tracingW, 13, ModStatusHover.tracingLines(status));
    }

    static int pill(
            GuiGraphics graphics, Font font, int x, int y, String text, int fill, int ink, boolean rightAlign) {
        int width = font.width(text) + 10;
        int left = rightAlign ? x - width : x;
        graphics.fill(left, y, left + width, y + 13, fill);
        graphics.renderOutline(left, y, width, 13, 0x44000000);
        graphics.drawString(font, text, left + 5, y + 3, ink, false);
        return width;
    }

    static void tabUnderline(GuiGraphics graphics, int x, int y, int width) {
        graphics.fill(x, y + 19, x + width, y + 21, LENS);
    }

    static void row(GuiGraphics graphics, int x, int y, int width, int height, boolean selected, boolean hovering) {
        if (selected) {
            graphics.fill(x, y, x + width, y + height, ROW);
        } else if (hovering) {
            graphics.fill(x, y, x + width, y + height, HOVER);
        }
    }

    static void card(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            int width,
            int height,
            String label,
            String value,
            int valueColor,
            List<String> tooltip) {
        well(graphics, x, y, width, height);
        graphics.drawString(font, label, x + 8, y + 8, DIM, false);
        graphics.drawString(font, value, x + 8, y + 22, valueColor, false);
        if (tooltip != null && !tooltip.isEmpty()) {
            addHit(x, y, width, height, tooltip);
        }
    }

    static String sessionLabel(TraceSessionSummary session) {
        if (session == null || !session.restarted()) {
            return session == null ? "" : session.sessionId();
        }
        return session.sessionId() + "  " + session.restartBadge();
    }

    static void section(GuiGraphics graphics, Font font, String title, int x, int y) {
        graphics.drawString(font, title, x, y, BRASS, false);
    }

    static int footerX(Frame frame, int index, int count, int buttonW) {
        if (count <= 1) {
            return frame.contentX + (frame.contentW - buttonW) / 2;
        }
        int extra = Math.max(0, frame.contentW - count * buttonW);
        int gap = extra / (count - 1);
        return frame.contentX + index * (buttonW + gap);
    }

    static EditBox search(EventLensScreen screen, Frame frame, String value, String hint, Consumer<String> onChange) {
        EditBox box = new EditBox(screen.textFont(), frame.contentX, frame.contentY, frame.contentW, 18, Component.literal(hint));
        box.setHint(Component.literal(hint));
        box.setValue(value);
        box.setMaxLength(64);
        box.setResponder(onChange);
        screen.add(box);
        return box;
    }

    private static String lastClickId = "";
    private static long lastClickAt;

    static boolean doubleClicked(String id) {
        long now = System.currentTimeMillis();
        boolean twice = id.equals(lastClickId) && now - lastClickAt <= 350L;
        lastClickId = id;
        lastClickAt = now;
        return twice;
    }

    static boolean matches(String query, String... parts) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        for (String part : parts) {
            if (part != null && part.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
