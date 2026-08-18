package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ModTraceFormatter {

    private ModTraceFormatter() {}

    public static List<ModChatLine> start(ModTraceResults.StartResult result) {
        if (result.needsHotConfirm()) {
            return List.of(
                    ModChatLine.text(result.eventSimpleName() + " fires constantly.", ModChatColor.YELLOW),
                    ModChatLine.text("A short bounded session is safer.", ModChatColor.WHITE),
                    ModChatLine.blank(),
                    ModChatLine.builder()
                            .click(
                                    "[Start anyway]",
                                    ModChatColor.AQUA,
                                    "/eventlens trace start " + result.eventSimpleName() + " --confirm-hot",
                                    "Start a bounded hot-event session")
                            .add("   ", ModChatColor.DARK_GRAY)
                            .click("[Cancel]", ModChatColor.WHITE, "/eventlens status", "Back to status")
                            .build());
        }
        if (!result.success()) {
            return List.of(ModChatLine.text(result.message(), ModChatColor.RED));
        }
        return List.of(
                ModChatLine.text("Session started", ModChatColor.GREEN),
                ModChatLine.builder()
                        .add("Id       ", ModChatColor.WHITE)
                        .click(result.sessionId(), ModChatColor.YELLOW, "/eventlens trace view " + result.sessionId(), "Open this session")
                        .build(),
                ModChatLine.builder()
                        .add("Event    ", ModChatColor.WHITE)
                        .add(result.eventSimpleName(), ModChatColor.AQUA)
                        .build(),
                ModChatLine.blank(),
                ModChatLine.text("Trigger that event, then open the session.", ModChatColor.WHITE),
                ModChatLine.blank(),
                ModChatLine.builder()
                        .click("[Open session]", ModChatColor.AQUA, "/eventlens trace view " + result.sessionId(), "View captured dispatches")
                        .add("   ", ModChatColor.DARK_GRAY)
                        .click("[Stop]", ModChatColor.AQUA, "/eventlens trace stop " + result.sessionId(), "Stop this session")
                        .add("   ", ModChatColor.DARK_GRAY)
                        .click("[Pause]", ModChatColor.AQUA, "/eventlens trace pause " + result.sessionId(), "Pause capture")
                        .build());
    }

    public static List<ModChatLine> stop(ModTraceResults.StopResult result) {
        if (!result.success()) {
            return List.of(ModChatLine.text(result.message(), ModChatColor.YELLOW));
        }
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text("Stopped " + result.sessionIds().size() + " session(s).", ModChatColor.GREEN));
        lines.add(ModChatLine.blank());
        for (String sessionId : result.sessionIds()) {
            lines.add(ModChatLine.builder()
                    .click(sessionId, ModChatColor.YELLOW, "/eventlens trace view " + sessionId, "View captured dispatches")
                    .add("   ", ModChatColor.DARK_GRAY)
                    .click("[Export]", ModChatColor.AQUA, "/eventlens trace export " + sessionId, "Export this session")
                    .build());
        }
        return lines;
    }

    public static List<ModChatLine> pause(ModTraceResults.PauseResult result) {
        if (!result.success()) {
            return List.of(ModChatLine.text(result.message(), ModChatColor.YELLOW));
        }
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text(result.message(), ModChatColor.GREEN));
        lines.add(ModChatLine.blank());
        for (String sessionId : result.sessionIds()) {
            String next = result.paused() ? "resume" : "pause";
            lines.add(ModChatLine.builder()
                    .click(sessionId, ModChatColor.YELLOW, "/eventlens trace view " + sessionId, "View captured dispatches")
                    .add("   ", ModChatColor.DARK_GRAY)
                    .click(
                            result.paused() ? "[Resume]" : "[Pause]",
                            ModChatColor.AQUA,
                            "/eventlens trace " + next + " " + sessionId,
                            result.paused() ? "Resume capture" : "Pause capture")
                    .build());
        }
        return lines;
    }

    public static List<ModChatLine> list(List<TraceSessionSummary> sessions) {
        if (sessions.isEmpty()) {
            return List.of(
                    ModChatLine.text("No sessions yet.", ModChatColor.WHITE),
                    ModChatLine.blank(),
                    ModChatLine.builder()
                            .click("[Events]", ModChatColor.AQUA, "/eventlens listeners", "Pick an event to trace")
                            .build());
        }
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text("Sessions", ModChatColor.GOLD));
        for (TraceSessionSummary session : sessions) {
            lines.addAll(sessionCard(session));
        }
        lines.add(ModChatLine.blank());
        lines.add(ModChatLine.text("Stop sessions when you are done. They add overhead.", ModChatColor.YELLOW));
        return lines;
    }

    public static List<ModChatLine> restart(ModTraceResults.RestartResult result) {
        if (!result.success()) {
            return List.of(ModChatLine.text(result.message(), ModChatColor.YELLOW));
        }
        return List.of(
                ModChatLine.text(result.message(), ModChatColor.GREEN),
                ModChatLine.blank(),
                ModChatLine.builder()
                        .click(
                                "[Open session]",
                                ModChatColor.AQUA,
                                "/eventlens trace view " + result.sessionId(),
                                "View the restarted session")
                        .build());
    }

    public static List<ModChatLine> export(ModTraceResults.ExportResult result) {
        if (!result.success()) {
            return List.of(ModChatLine.text(result.message(), ModChatColor.RED));
        }
        Path absolute = Path.of(result.path()).toAbsolutePath().normalize();
        Path folder = absolute.getParent();
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text("Exported " + result.dispatchCount() + " dispatch(es).", ModChatColor.GREEN));
        lines.add(pathLine("Saved to", absolute.toString(), "Click to copy file path"));
        if (folder != null) {
            lines.add(pathLine("Folder", folder.toString(), "Click to copy folder path"));
        }
        lines.add(ModChatLine.blank());
        lines.add(ModChatLine.builder()
                .click("[Open session]", ModChatColor.AQUA, "/eventlens trace view " + result.sessionId(), "View this session")
                .build());
        return lines;
    }

    private static ModChatLine pathLine(String label, String path, String hoverText) {
        return ModChatLine.builder()
                .add(label + ": ", ModChatColor.GRAY)
                .copy(path, ModChatColor.AQUA, path, hoverText)
                .build();
    }

    public static List<ModChatLine> uiUnavailable() {
        return List.of(ModChatLine.text(
                "Open the EventLens screen with /eventlens ui on Fabric, NeoForge, and Forge.",
                ModChatColor.YELLOW));
    }

    public static List<ModChatLine> usage() {
        return List.of(
                ModChatLine.text("EventLens client", ModChatColor.GOLD),
                ModChatLine.text("/eventlens status", ModChatColor.WHITE),
                ModChatLine.text("/eventlens ui", ModChatColor.WHITE),
                ModChatLine.text("/eventlens listeners", ModChatColor.WHITE),
                ModChatLine.text("/eventlens mod <id>", ModChatColor.WHITE),
                ModChatLine.text("/eventlens exceptions", ModChatColor.WHITE),
                ModChatLine.text("/eventlens trace start <event>", ModChatColor.WHITE));
    }

    public static List<ModChatLine> traceUsage() {
        return List.of(
                ModChatLine.text("Trace commands", ModChatColor.GOLD),
                ModChatLine.text("start <event>   begin recording", ModChatColor.WHITE),
                ModChatLine.text("view <id> [--run n]  open a session or earlier run", ModChatColor.WHITE),
                ModChatLine.text("stop [id]       stop recording", ModChatColor.WHITE),
                ModChatLine.text("pause [id]      keep the session, stop capture", ModChatColor.WHITE),
                ModChatLine.text("resume [id]     start capturing again", ModChatColor.WHITE),
                ModChatLine.text("restart <id>    reuse the same id; keep previous runs", ModChatColor.WHITE),
                ModChatLine.text("list            all sessions", ModChatColor.WHITE),
                ModChatLine.text(
                        "export [id] [--format json|ndjson|text|html] [--shareable|--full]",
                        ModChatColor.WHITE));
    }

    static String startCommand(String eventSimpleName) {
        if (SupportedModEventTypes.isHot(eventSimpleName)) {
            return "/eventlens trace start " + eventSimpleName + " --confirm-hot";
        }
        return "/eventlens trace start " + eventSimpleName;
    }

    static List<ModChatLine> sessionCard(TraceSessionSummary session) {
        return ModTraceSessionCards.sessionCard(session);
    }
}
