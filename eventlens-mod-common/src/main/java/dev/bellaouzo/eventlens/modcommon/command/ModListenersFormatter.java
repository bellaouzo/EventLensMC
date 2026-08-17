package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ModListenersFormatter {

    private static final int MAX_HANDLERS = 12;

    private ModListenersFormatter() {}

    public static List<ModChatLine> listeners(String eventSimpleName, List<ModHandlerRegistration> handlers) {
        if (eventSimpleName == null || eventSimpleName.isBlank()) {
            return catalog();
        }
        if (!SupportedModEventTypes.isSupportedSimpleName(eventSimpleName)) {
            return List.of(ModChatLine.text("Unknown event. Click [Events] to browse.", ModChatColor.RED));
        }
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text(eventSimpleName, ModChatColor.GOLD));
        String summary = SupportedModEventTypes.summary(eventSimpleName);
        if (!summary.isBlank()) {
            lines.add(ModChatLine.text(summary, ModChatColor.WHITE));
        }
        if (SupportedModEventTypes.isHot(eventSimpleName)) {
            lines.add(ModChatLine.text("Hot event — keep sessions short.", ModChatColor.YELLOW));
        }
        lines.add(ModChatLine.blank());
        lines.addAll(handlerSection(handlers));
        lines.add(ModChatLine.blank());
        lines.add(ModChatLine.builder()
                .click("[Start trace]", ModChatColor.AQUA, ModTraceFormatter.startCommand(eventSimpleName), "Start tracing " + eventSimpleName)
                .add("   ", ModChatColor.DARK_GRAY)
                .click("[Events]", ModChatColor.AQUA, "/eventlens listeners", "Browse client events")
                .build());
        return lines;
    }

    private static List<ModChatLine> catalog() {
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text("Client events", ModChatColor.GOLD));
        lines.add(ModChatLine.text("Click a name to see which mods subscribe.", ModChatColor.WHITE));
        for (String name : SupportedModEventTypes.simpleNames()) {
            lines.add(ModChatLine.blank());
            lines.add(ModChatLine.builder()
                    .click(name, ModChatColor.AQUA, "/eventlens listeners " + name, "Inspect " + name)
                    .add(SupportedModEventTypes.isHot(name) ? "  hot" : "", ModChatColor.YELLOW)
                    .build());
            lines.add(ModChatLine.text("  " + SupportedModEventTypes.summary(name), ModChatColor.WHITE));
            lines.add(ModChatLine.builder()
                    .add("  ", ModChatColor.WHITE)
                    .click("[Start]", ModChatColor.AQUA, ModTraceFormatter.startCommand(name), "Start tracing " + name)
                    .build());
        }
        return lines;
    }

    private static List<ModChatLine> handlerSection(List<ModHandlerRegistration> handlers) {
        List<ModHandlerRegistration> others = handlers.stream()
                .filter(handler -> !"eventlens".equals(handler.modId()))
                .toList();
        if (others.isEmpty()) {
            return List.of(
                    ModChatLine.text("Subscribers", ModChatColor.GOLD),
                    ModChatLine.text("None listed from @SubscribeEvent scans.", ModChatColor.YELLOW),
                    ModChatLine.text("Mods that register with addListener() will not appear.", ModChatColor.WHITE),
                    ModChatLine.text("A trace still records the event itself.", ModChatColor.WHITE));
        }
        Set<String> mods = new LinkedHashSet<>();
        for (ModHandlerRegistration handler : others) {
            mods.add(handler.modId());
        }
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text("Subscribers  " + others.size() + " handler(s)  " + mods.size() + " mod(s)", ModChatColor.GOLD));
        int shown = 0;
        for (ModHandlerRegistration handler : others) {
            if (shown >= MAX_HANDLERS) {
                lines.add(ModChatLine.text("  …", ModChatColor.WHITE));
                break;
            }
            lines.add(ModChatLine.text("  " + handler.modId(), ModChatColor.AQUA));
            lines.add(ModChatLine.text(
                    "    " + simpleClass(handler.handlerClassName()) + "#" + handler.methodName()
                            + "  " + priorityLabel(handler.priority()),
                    ModChatColor.WHITE));
            shown++;
        }
        if (mods.size() >= 2) {
            lines.add(ModChatLine.blank());
            lines.add(ModChatLine.text("Overlap", ModChatColor.GOLD));
            lines.add(ModChatLine.text(String.join(", ", mods), ModChatColor.YELLOW));
            lines.add(ModChatLine.text("These mods all listen. If the event misbehaves, start here.", ModChatColor.WHITE));
        }
        return lines;
    }

    private static String simpleClass(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot < 0 ? className : className.substring(lastDot + 1);
    }

    private static String priorityLabel(int priority) {
        return switch (priority) {
            case 0 -> "HIGHEST";
            case 1 -> "HIGH";
            case 2 -> "NORMAL";
            case 3 -> "LOW";
            case 4 -> "LOWEST";
            default -> "p" + priority;
        };
    }
}
