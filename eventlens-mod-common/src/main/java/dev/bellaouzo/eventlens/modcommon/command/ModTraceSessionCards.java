package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.util.List;

final class ModTraceSessionCards {

    private ModTraceSessionCards() {}

    static List<ModChatLine> sessionCard(TraceSessionSummary session) {
        String eventName = SupportedModEventTypes.displaySimpleName(session.eventClassName());
        boolean terminal = session.state().isTerminal();
        boolean paused = session.state() == TraceSessionState.PAUSED;
        return List.of(
                ModChatLine.blank(),
                ModChatLine.builder()
                        .click(
                                session.sessionId(),
                                ModChatColor.YELLOW,
                                "/eventlens trace view " + session.sessionId(),
                                "Open this session")
                        .add("  " + session.state(), ModChatColor.WHITE)
                        .add(session.restarted() ? "  " + session.restartBadge() : "", ModChatColor.GOLD)
                        .build(),
                ModChatLine.builder()
                        .add("  ", ModChatColor.WHITE)
                        .click(
                                eventName,
                                ModChatColor.AQUA,
                                "/eventlens listeners " + eventName,
                                "Mods that subscribe to " + eventName)
                        .build(),
                ModChatLine.text(
                        "  " + session.capturedEvents() + " captured   " + session.droppedEvents() + " dropped",
                        ModChatColor.WHITE),
                actions(session.sessionId(), terminal, paused));
    }

    private static ModChatLine actions(String sessionId, boolean terminal, boolean paused) {
        if (terminal) {
            return ModChatLine.builder()
                    .add("  ", ModChatColor.WHITE)
                    .click("[Open]", ModChatColor.AQUA, "/eventlens trace view " + sessionId, "View captured dispatches")
                    .add("   ", ModChatColor.DARK_GRAY)
                    .click(
                            "[Restart]",
                            ModChatColor.AQUA,
                            "/eventlens trace restart " + sessionId,
                            "Restart this session id and clear captures")
                    .build();
        }
        return ModChatLine.builder()
                .add("  ", ModChatColor.WHITE)
                .click("[Open]", ModChatColor.AQUA, "/eventlens trace view " + sessionId, "View captured dispatches")
                .add("   ", ModChatColor.DARK_GRAY)
                .click(
                        paused ? "[Resume]" : "[Pause]",
                        ModChatColor.AQUA,
                        "/eventlens trace " + (paused ? "resume " : "pause ") + sessionId,
                        paused ? "Resume capture" : "Pause capture")
                .add("   ", ModChatColor.DARK_GRAY)
                .click("[Stop]", ModChatColor.AQUA, "/eventlens trace stop " + sessionId, "Stop this session")
                .build();
    }
}
