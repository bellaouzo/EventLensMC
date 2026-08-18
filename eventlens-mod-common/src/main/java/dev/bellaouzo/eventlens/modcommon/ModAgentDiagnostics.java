package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.application.AgentAttachDiagnostics;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.util.ArrayList;
import java.util.List;

public final class ModAgentDiagnostics {

    private ModAgentDiagnostics() {}

    public static void logStartupWarnings(boolean agentAttached) {
        for (AgentAttachDiagnostics.Line line : AgentAttachDiagnostics.diagnose(
                        AgentAttachDiagnostics.Role.CLIENT, agentAttached)
                .lines()) {
            System.out.println("[EventLens] " + line.level().toUpperCase() + ": " + line.message());
        }
    }

    public static List<ModChatLine> statusLines(boolean agentAttached) {
        List<ModChatLine> lines = new ArrayList<>();
        for (AgentAttachDiagnostics.Line line :
                AgentAttachDiagnostics.diagnose(AgentAttachDiagnostics.Role.CLIENT, agentAttached)
                        .lines()) {
            lines.add(ModChatLine.text(line.message(), colorFor(line.level())));
        }
        return lines;
    }

    private static ModChatColor colorFor(String level) {
        return switch (level) {
            case "error" -> ModChatColor.RED;
            case "warn" -> ModChatColor.YELLOW;
            default -> ModChatColor.GRAY;
        };
    }
}
