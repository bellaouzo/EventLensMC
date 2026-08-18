package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.domain.instrumentation.AgentInstallHints;
import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModAgentDiagnostics;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatSpan;
import java.util.ArrayList;
import java.util.List;

public final class ModStatusFormatter {

    private ModStatusFormatter() {}

    public static List<ModChatLine> render(ModTraceResults.Status status, ModRuntimeKind runtimeKind) {
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
            lines.addAll(ModAgentDiagnostics.statusLines(status.agentPresent()));
            lines.addAll(agentSetupLines(status, runtimeKind));
        }
        if (!status.sessions().isEmpty()) {
            lines.add(ModChatLine.blank());
            lines.add(ModChatLine.text("Open sessions", ModChatColor.GOLD));
            for (TraceSessionSummary session : status.sessions()) {
                lines.addAll(ModTraceFormatter.sessionCard(session));
            }
        }
        lines.add(ModChatLine.blank());
        ModChatLine.Builder actions = ModChatLine.builder()
                .click("[Open UI]", ModChatColor.AQUA, "/eventlens ui", "Open the EventLens screen")
                .add("   ", ModChatColor.DARK_GRAY)
                .click("[Events]", ModChatColor.AQUA, "/eventlens listeners", "Browse client events and mod overlap")
                .add("   ", ModChatColor.DARK_GRAY)
                .click("[Sessions]", ModChatColor.AQUA, "/eventlens trace list", "Open captured sessions");
        if (!status.agentPresent()) {
            if (runtimeKind != ModRuntimeKind.FABRIC) {
                String jvmArg = AgentInstallHints.clientJvmArgument(status.version());
                actions.add("   ", ModChatColor.DARK_GRAY)
                        .copy("[Copy JVM arg]", ModChatColor.AQUA, jvmArg, "Copy client -javaagent argument");
            }
            actions.add("   ", ModChatColor.DARK_GRAY)
                    .copy("[Agent guide]", ModChatColor.AQUA, AgentInstallHints.README_URL, "Copy README install link");
        }
        lines.add(actions.build());
        return lines;
    }

    private static List<ModChatLine> agentSetupLines(ModTraceResults.Status status, ModRuntimeKind runtimeKind) {
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text("Client agent not attached — per-mod handler timing unavailable.", ModChatColor.YELLOW));
        lines.add(ModChatLine.text(
                "Do NOT put eventlens-client-agent in mods/ — launchers reject it as not a mod.",
                ModChatColor.RED));
        if (runtimeKind == ModRuntimeKind.FABRIC) {
            lines.add(ModChatLine.text(
                    "Fabric client agent is not supported yet. Dispatch timing still works.", ModChatColor.GRAY));
        } else {
            String jvmArg = AgentInstallHints.clientJvmArgument(status.version());
            lines.add(ModChatLine.builder()
                    .add("Launcher JVM arg (not mods/): ", ModChatColor.WHITE)
                    .copy(jvmArg, ModChatColor.AQUA, jvmArg, "Copy client -javaagent argument")
                    .build());
            lines.add(ModChatLine.text(
                    "Optional: place "
                            + AgentInstallHints.observabilityJarName(status.version())
                            + " next to the agent jar (fat agent jar also works).",
                    ModChatColor.GRAY));
        }
        lines.add(ModChatLine.text(
                "After restart, /eventlens status should show precise.", ModChatColor.GRAY));
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
