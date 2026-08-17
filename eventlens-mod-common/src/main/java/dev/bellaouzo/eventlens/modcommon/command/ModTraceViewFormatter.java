package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ModTraceViewFormatter {

    private ModTraceViewFormatter() {}

    public static List<ModChatLine> view(ModTraceResults.ViewResult result, List<ModHandlerRegistration> handlers) {
        if (result.kind() != ModTraceResults.ViewResult.Kind.SUCCESS) {
            return List.of(ModChatLine.text(result.message(), ModChatColor.RED));
        }
        TraceSessionSummary summary = result.summary();
        String eventName = SupportedModEventTypes.displaySimpleName(summary.eventClassName());
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text("Session", ModChatColor.GOLD));
        lines.add(ModChatLine.builder()
                .add("Id       ", ModChatColor.WHITE)
                .click(summary.sessionId(), ModChatColor.YELLOW, "/eventlens trace list", "Back to session list")
                .add("  " + summary.state(), ModChatColor.WHITE)
                .build());
        lines.add(ModChatLine.builder()
                .add("Event    ", ModChatColor.WHITE)
                .click(eventName, ModChatColor.AQUA, "/eventlens listeners " + eventName, "Mods that subscribe to " + eventName)
                .build());
        lines.add(ModChatLine.text(
                "Captured " + summary.capturedEvents() + "   page " + result.page() + " of " + result.totalPages(),
                ModChatColor.WHITE));
        lines.addAll(overlapLines(handlers));
        lines.addAll(ModTraceTimingFormatter.conflictLines(result.records()));
        lines.add(ModChatLine.blank());
        if (result.records().isEmpty()) {
            lines.add(ModChatLine.text("Nothing captured yet.", ModChatColor.YELLOW));
            lines.add(ModChatLine.text("Trigger " + eventName + ", then refresh.", ModChatColor.WHITE));
        } else {
            for (TraceDispatchRecord record : result.records()) {
                lines.addAll(dispatchBlock(summary.sessionId(), record, result.focused()));
            }
        }
        lines.add(ModChatLine.blank());
        if (result.focused()) {
            lines.add(ModChatLine.builder()
                    .click("[Back]", ModChatColor.AQUA, "/eventlens trace view " + summary.sessionId(), "Back to session")
                    .build());
        } else {
            lines.add(viewNavigation(summary.sessionId(), result.page(), result.totalPages()));
        }
        lines.add(ModChatLine.builder()
                .click("[Stop]", ModChatColor.AQUA, "/eventlens trace stop " + summary.sessionId(), "Stop this session")
                .add("   ", ModChatColor.DARK_GRAY)
                .click("[Export]", ModChatColor.AQUA, "/eventlens trace export " + summary.sessionId(), "Export this session")
                .build());
        return lines;
    }

    private static List<ModChatLine> overlapLines(List<ModHandlerRegistration> handlers) {
        Set<String> mods = new LinkedHashSet<>();
        for (ModHandlerRegistration handler : handlers) {
            if (!"eventlens".equals(handler.modId())) {
                mods.add(handler.modId());
            }
        }
        if (mods.size() < 2) {
            return List.of();
        }
        return List.of(
                ModChatLine.blank(),
                ModChatLine.text("Mods that also subscribe", ModChatColor.GOLD),
                ModChatLine.text(String.join(", ", mods), ModChatColor.YELLOW),
                ModChatLine.text("If this event misbehaves, start with those mods.", ModChatColor.WHITE));
    }

    private static ModChatLine viewNavigation(String sessionId, int page, int totalPages) {
        ModChatLine.Builder builder = ModChatLine.builder();
        if (page > 1) {
            builder.click("[Prev]", ModChatColor.AQUA, "/eventlens trace view " + sessionId + " " + (page - 1), "Previous page");
            builder.add("   ", ModChatColor.DARK_GRAY);
        }
        builder.add("Page " + page + " of " + totalPages, ModChatColor.YELLOW);
        if (page < totalPages) {
            builder.add("   ", ModChatColor.DARK_GRAY);
            builder.click("[Next]", ModChatColor.AQUA, "/eventlens trace view " + sessionId + " " + (page + 1), "Next page");
        }
        builder.add("   ", ModChatColor.DARK_GRAY);
        builder.click("[Refresh]", ModChatColor.AQUA, "/eventlens trace view " + sessionId + " " + page, "Reload this page");
        return builder.build();
    }

    private static List<ModChatLine> dispatchBlock(String sessionId, TraceDispatchRecord record, boolean focused) {
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.builder()
                .click(
                        "Dispatch #" + record.sequence(),
                        ModChatColor.AQUA,
                        "/eventlens trace view " + sessionId + " --dispatch " + record.sequence(),
                        "Open this dispatch")
                .add("   " + String.format(Locale.ROOT, "%.2f ms", record.durationNanos() / 1_000_000.0), ModChatColor.WHITE)
                .build());
        List<SnapshotField> fields = record.snapshotAfter().fields();
        int limit = focused ? 16 : 6;
        int shown = 0;
        if (fields != null) {
            for (SnapshotField field : fields) {
                if (shown >= limit) {
                    lines.add(ModChatLine.text("    …", ModChatColor.WHITE));
                    break;
                }
                lines.add(ModChatLine.builder()
                        .add("    " + field.name(), ModChatColor.YELLOW)
                        .add("  " + displayValue(field.value()), ModChatColor.WHITE)
                        .build());
                shown++;
            }
        }
        if (shown == 0) {
            lines.add(ModChatLine.text("    no fields", ModChatColor.WHITE));
        }
        lines.addAll(ModTraceTimingFormatter.listenerLines(record, focused));
        return lines;
    }

    private static String displayValue(SnapshotValue value) {
        return switch (value) {
            case SnapshotValue.Present present -> present.display();
            case SnapshotValue.Truncated truncated -> truncated.display();
            case SnapshotValue.Unsupported unsupported -> "?" + unsupported.reason();
        };
    }
}
