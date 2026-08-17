package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatSpan;
import java.util.ArrayList;
import java.util.List;

public final class ModStatusFormatter {

    private ModStatusFormatter() {}

    public static List<ModChatLine> render(ModTraceResults.Status status) {
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.builder()
                .add("◆ ", ModChatColor.GOLD)
                .add(ModChatSpan.titled("EventLens", ModChatColor.GOLD, "Client diagnostics"))
                .add("  v" + status.version(), ModChatColor.YELLOW)
                .build());
        lines.add(ModChatLine.blank());
        lines.add(labeled("Platform", status.platform(), ModChatColor.AQUA));
        lines.add(labeled("Minecraft", status.minecraftVersion(), ModChatColor.AQUA));
        lines.add(ModChatLine.builder()
                .add("Tracing    ", ModChatColor.WHITE)
                .add(status.tracingEnabled() ? "on" : "off", status.tracingEnabled() ? ModChatColor.GREEN : ModChatColor.RED)
                .build());
        lines.add(ModChatLine.builder()
                .add(pad("Instr."), ModChatColor.WHITE)
                .add(
                        status.agentPresent() ? "precise" : "dispatch-only",
                        status.agentPresent() ? ModChatColor.GREEN : ModChatColor.YELLOW)
                .build());
        lines.add(sessionsLine(status));
        lines.add(ModChatLine.blank());
        lines.add(ModChatLine.text("Records client events and lists mods that subscribe to them.", ModChatColor.WHITE));
        if (status.agentPresent()) {
            lines.add(ModChatLine.text(
                    "The client agent times other mods' game-bus handlers.", ModChatColor.WHITE));
        } else {
            lines.add(ModChatLine.text(
                    "It cannot time another mod's handler without the client Java agent.", ModChatColor.YELLOW));
        }
        if (!status.sessions().isEmpty()) {
            lines.add(ModChatLine.blank());
            lines.add(ModChatLine.text("Open sessions", ModChatColor.GOLD));
            for (TraceSessionSummary session : status.sessions()) {
                lines.addAll(ModTraceFormatter.sessionCard(session));
            }
        }
        lines.add(ModChatLine.blank());
        lines.add(ModChatLine.builder()
                .click("[Open UI]", ModChatColor.AQUA, "/eventlens ui", "Open the EventLens screen")
                .add("   ", ModChatColor.DARK_GRAY)
                .click("[Events]", ModChatColor.AQUA, "/eventlens listeners", "Browse client events and mod overlap")
                .add("   ", ModChatColor.DARK_GRAY)
                .click("[Sessions]", ModChatColor.AQUA, "/eventlens trace list", "Open captured sessions")
                .build());
        return lines;
    }

    private static ModChatLine labeled(String label, String value, ModChatColor color) {
        return ModChatLine.builder()
                .add(pad(label), ModChatColor.WHITE)
                .add(value, color)
                .build();
    }

    private static String pad(String label) {
        return String.format("%-10s ", label);
    }

    private static ModChatLine sessionsLine(ModTraceResults.Status status) {
        ModChatLine.Builder builder = ModChatLine.builder().add(pad("Sessions"), ModChatColor.WHITE);
        if (status.activeSessionCount() > 0) {
            builder.click(
                    Integer.toString(status.activeSessionCount()) + " active",
                    ModChatColor.AQUA,
                    "/eventlens trace list",
                    "Open the session list");
        } else {
            builder.add("none", ModChatColor.WHITE);
        }
        return builder.build();
    }
}
