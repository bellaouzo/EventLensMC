package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;

public final class TraceReportTextSerializer {

    private TraceReportTextSerializer() {}

    public static String serialize(TraceReportDocument document) {
        StringBuilder text = new StringBuilder(4096);
        appendHeader(text, document);
        appendWarnings(text, document.warnings());
        appendSession(text, document);
        appendDispatches(text, document.dispatches());
        return text.toString();
    }

    public static String compact(TraceReportDocument document) {
        TraceSessionSummary summary = document.summary();
        StringBuilder text = new StringBuilder(512);
        text.append("EventLens trace ")
                .append(summary.sessionId())
                .append(" [")
                .append(summary.state())
                .append("] ")
                .append(simpleName(summary.eventClassName()))
                .append('\n');
        text.append("Captured ")
                .append(summary.capturedEvents())
                .append(" · dropped ")
                .append(summary.droppedEvents())
                .append(" · sampled ")
                .append(summary.sampledOutEvents())
                .append('\n');
        text.append("Duration ")
                .append(summary.lastActivityAtMillis() - summary.startedAtMillis())
                .append(" ms · filters: ")
                .append(TraceFilterFormatter.describe(document.filter()))
                .append('\n');
        text.append("Conflicts: ")
                .append(summary.conflictSummary().likelyConflictSummary())
                .append('\n');
        if (!document.warnings().isEmpty()) {
            text.append("Warnings: ")
                    .append(String.join("; ", document.warnings()))
                    .append('\n');
        }
        text.append("EventLens ")
                .append(document.environment().eventLensVersion())
                .append(" · Paper ")
                .append(document.environment().paperVersion())
                .append(" · Java ")
                .append(document.environment().javaVersion());
        return text.toString().trim();
    }

    public static String compactDispatch(TraceReportDocument document, TraceDispatchRecord dispatch) {
        TraceSessionSummary summary = document.summary();
        StringBuilder text = new StringBuilder(768);
        text.append("EventLens trace ")
                .append(summary.sessionId())
                .append(" dispatch #")
                .append(dispatch.sequence())
                .append('\n');
        text.append(simpleName(summary.eventClassName()))
                .append(" · ")
                .append(DurationStats.formatMillis(dispatch.durationNanos()))
                .append(" · listeners ")
                .append(dispatch.listenerChain().size())
                .append('\n');
        dispatch.playerName()
                .ifPresent(player -> text.append("player: ").append(player).append('\n'));
        dispatch.worldName()
                .ifPresent(world -> text.append("world: ").append(world).append('\n'));
        if (!dispatch.partialReasons().isEmpty()) {
            text.append("partial: ")
                    .append(ReportFormatting.formatPartialReasons(dispatch.partialReasons()))
                    .append('\n');
        }
        for (TraceListenerSnapshot listener : dispatch.listenerChain()) {
            text.append("[")
                    .append(listener.priority())
                    .append("] ")
                    .append(listener.pluginName())
                    .append('/')
                    .append(ListenerDisplayFormatter.format(
                            listener.pluginName(), listener.listenerClassName(), listener.methodName()))
                    .append('\n');
        }
        text.append("Redaction: ").append(document.redactionMode());
        return text.toString().trim();
    }

    private static void appendHeader(StringBuilder text, TraceReportDocument document) {
        TraceReportEnvironment environment = document.environment();
        text.append("EventLens Trace Report\n");
        text.append("======================\n");
        text.append("Generated: ")
                .append(ReportFormatting.formatGeneratedAt(environment.generatedAtMillis()))
                .append('\n');
        text.append("Redaction: ").append(document.redactionMode()).append('\n');
        text.append("Server: ").append(environment.serverVersion()).append('\n');
        text.append("Paper: ").append(environment.paperVersion()).append('\n');
        text.append("Java: ").append(environment.javaVersion()).append('\n');
        text.append("EventLens: ").append(environment.eventLensVersion()).append('\n');
        if (!environment.pluginVersions().isEmpty()) {
            text.append("Plugins:\n");
            environment.pluginVersions().forEach((name, version) -> text.append("  ")
                    .append(name)
                    .append(" ")
                    .append(version)
                    .append('\n'));
        }
        text.append('\n');
    }

    private static void appendWarnings(StringBuilder text, List<String> warnings) {
        if (warnings.isEmpty()) {
            return;
        }
        text.append("Warnings\n");
        text.append("--------\n");
        for (String warning : warnings) {
            text.append("- ").append(warning).append('\n');
        }
        text.append('\n');
    }

    private static void appendSession(StringBuilder text, TraceReportDocument document) {
        TraceSessionSummary summary = document.summary();
        text.append("Session ").append(summary.sessionId()).append('\n');
        text.append("Event: ").append(summary.eventClassName()).append('\n');
        text.append("State: ").append(summary.state()).append('\n');
        text.append("Owner: ").append(summary.ownerName()).append('\n');
        text.append("Duration: ")
                .append(summary.lastActivityAtMillis() - summary.startedAtMillis())
                .append(" ms\n");
        text.append("Filters: ")
                .append(TraceFilterFormatter.describe(document.filter()))
                .append('\n');
        text.append("Captured: ")
                .append(summary.capturedEvents())
                .append(" · Dropped: ")
                .append(summary.droppedEvents())
                .append(" · Sampled out: ")
                .append(summary.sampledOutEvents())
                .append('\n');
        text.append("Conflicts: ")
                .append(summary.conflictSummary().likelyConflictSummary())
                .append('\n');
        if (!summary.conflictSummary().suggestions().isEmpty()) {
            text.append("Suggestions:\n");
            for (String suggestion : summary.conflictSummary().suggestions()) {
                text.append("  - ").append(suggestion).append('\n');
            }
        }
        text.append('\n');
    }

    private static void appendDispatches(StringBuilder text, List<TraceDispatchRecord> dispatches) {
        if (dispatches.isEmpty()) {
            text.append("No captured dispatches.\n");
            return;
        }
        text.append("Dispatches\n");
        text.append("----------\n");
        for (TraceDispatchRecord dispatch : dispatches) {
            text.append('#')
                    .append(dispatch.sequence())
                    .append(" · ")
                    .append(DurationStats.formatMillis(dispatch.durationNanos()))
                    .append(" · listeners ")
                    .append(dispatch.listenerChain().size())
                    .append('\n');
            dispatch.playerName()
                    .ifPresent(
                            player -> text.append("  player: ").append(player).append('\n'));
            dispatch.worldName()
                    .ifPresent(world -> text.append("  world: ").append(world).append('\n'));
            if (!dispatch.partialReasons().isEmpty()) {
                text.append("  partial: ")
                        .append(ReportFormatting.formatPartialReasons(dispatch.partialReasons()))
                        .append('\n');
            }
            for (TraceListenerSnapshot listener : dispatch.listenerChain()) {
                text.append("  [")
                        .append(listener.priority())
                        .append("] ")
                        .append(listener.pluginName())
                        .append('/')
                        .append(ListenerDisplayFormatter.format(
                                listener.pluginName(), listener.listenerClassName(), listener.methodName()))
                        .append('\n');
            }
            text.append('\n');
        }
    }

    private static String simpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
}
