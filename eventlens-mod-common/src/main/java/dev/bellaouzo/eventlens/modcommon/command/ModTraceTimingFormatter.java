package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.application.SessionConflictAnalyzer;
import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.observability.PerformanceBudget;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ModTraceTimingFormatter {

    private ModTraceTimingFormatter() {}

    static List<ModChatLine> listenerLines(TraceDispatchRecord record, boolean focused) {
        List<ListenerTimingRecord> timings = record.listenerTimings();
        if (timings == null || timings.isEmpty()) {
            return List.of();
        }
        int limit = focused ? 16 : 8;
        List<ModChatLine> lines = new ArrayList<>();
        int shown = 0;
        for (ListenerTimingRecord timing : timings) {
            if (shown >= limit) {
                lines.add(ModChatLine.text("    …", ModChatColor.WHITE));
                break;
            }
            lines.add(listenerLine(timing));
            shown++;
        }
        return lines;
    }

    static List<ModChatLine> conflictLines(List<TraceDispatchRecord> records) {
        SessionConflictSummary summary =
                SessionConflictAnalyzer.analyze(records, PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS);
        if (summary.dispatchesWithConflicts() <= 0) {
            return List.of();
        }
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.blank());
        lines.add(ModChatLine.text("Conflicts", ModChatColor.GOLD));
        lines.add(ModChatLine.text(summary.likelyConflictSummary(), ModChatColor.YELLOW));
        return lines;
    }

    private static ModChatLine listenerLine(ListenerTimingRecord timing) {
        String method = simpleName(timing.listenerClassName()) + "#" + timing.methodName();
        String duration = String.format(Locale.ROOT, "%.2f ms", timing.durationNanos() / 1_000_000.0);
        ModChatLine.Builder builder = ModChatLine.builder()
                .add("    #" + timing.invocationOrder() + "  ", ModChatColor.WHITE)
                .add(timing.pluginName(), ModChatColor.AQUA)
                .add("  " + method, ModChatColor.YELLOW)
                .add("  " + duration, ModChatColor.WHITE);
        timing.cancellationTransition().ifPresent(transition -> {
            if (transition.kind() == CancellationTransitionKind.BECAME_CANCELLED) {
                builder.add("  cancelled", ModChatColor.RED);
            } else if (transition.kind() == CancellationTransitionKind.BECAME_UNCANCELLED) {
                builder.add("  uncancelled", ModChatColor.GREEN);
            }
        });
        return builder.build();
    }

    private static String simpleName(String className) {
        int dot = className.lastIndexOf('.');
        return dot < 0 ? className : className.substring(dot + 1);
    }
}
