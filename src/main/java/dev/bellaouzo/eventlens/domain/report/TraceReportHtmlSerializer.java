package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Map;

public final class TraceReportHtmlSerializer {

    private static final String TABLE_END = "</tbody></table>";
    private static final String SECTION_END = "</section>";
    private static final String CELL_DIVIDER = "</td><td>";
    private static final String ROW_END = "</td></tr>";
    private static final String LIST_ITEM_END = "</li>";
    private static final String STYLES =
            """
            :root {
              --bg: #f8fafc;
              --surface: #ffffff;
              --border: #dbe3ef;
              --text: #0f172a;
              --muted: #64748b;
              --accent: #2563eb;
              --accent-soft: #dbeafe;
              --warn-bg: #fff7ed;
              --warn-border: #fdba74;
              --warn-text: #9a3412;
              --ok-bg: #ecfdf5;
              --ok-text: #047857;
              --mono: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            }
            * { box-sizing: border-box; }
            body {
              margin: 0;
              font-family: "Segoe UI", system-ui, sans-serif;
              background: linear-gradient(180deg, #eef4ff 0%, var(--bg) 220px);
              color: var(--text);
              line-height: 1.5;
            }
            .page { max-width: 1080px; margin: 0 auto; padding: 32px 24px 48px; }
            .hero {
              background: var(--surface);
              border: 1px solid var(--border);
              border-radius: 16px;
              padding: 24px 28px;
              box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
              margin-bottom: 20px;
            }
            .hero h1 { margin: 0 0 8px; font-size: 2rem; letter-spacing: -0.02em; }
            .hero .subtitle { color: var(--muted); margin: 0; }
            .badges { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 14px; }
            .badge {
              display: inline-block;
              padding: 4px 10px;
              border-radius: 999px;
              font-size: 0.85rem;
              font-weight: 600;
              background: var(--accent-soft);
              color: var(--accent);
            }
            .badge.ok { background: var(--ok-bg); color: var(--ok-text); }
            .badge.warn { background: var(--warn-bg); color: var(--warn-text); }
            .card {
              background: var(--surface);
              border: 1px solid var(--border);
              border-radius: 14px;
              padding: 20px 22px;
              margin-bottom: 18px;
              box-shadow: 0 4px 18px rgba(15, 23, 42, 0.04);
            }
            .card h2 {
              margin: 0 0 14px;
              font-size: 1.15rem;
              color: var(--accent);
              letter-spacing: -0.01em;
            }
            .card h3 {
              margin: 18px 0 10px;
              font-size: 1rem;
              color: var(--text);
            }
            table { border-collapse: collapse; width: 100%; }
            th, td {
              border: 1px solid var(--border);
              padding: 10px 12px;
              text-align: left;
              vertical-align: top;
            }
            th {
              width: 220px;
              background: #f1f5f9;
              color: #334155;
              font-weight: 600;
            }
            thead th { width: auto; background: #e2e8f0; }
            tbody tr:nth-child(even) td { background: #fbfdff; }
            .warn-list { display: grid; gap: 8px; }
            .warn {
              background: var(--warn-bg);
              border: 1px solid var(--warn-border);
              color: var(--warn-text);
              padding: 10px 12px;
              border-radius: 10px;
            }
            .mono { font-family: var(--mono); font-size: 0.92rem; word-break: break-word; }
            .dispatch-meta { margin: 0 0 10px; padding-left: 18px; color: var(--muted); }
            .dispatch-meta li { margin: 4px 0; }
            """;

    private TraceReportHtmlSerializer() {}

    public static String serialize(TraceReportDocument document) {
        StringBuilder html = new StringBuilder(8192);
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"><meta name=\"viewport\"")
                .append(" content=\"width=device-width, initial-scale=1\"><title>EventLens Trace ")
                .append(escape(document.summary().sessionId()))
                .append("</title><style>")
                .append(STYLES)
                .append("</style></head><body><div class=\"page\">");
        appendHero(html, document);
        appendWarnings(html, document.warnings());
        appendSession(html, document);
        appendDispatches(html, document.dispatches());
        html.append("</div></body></html>");
        return html.toString();
    }

    private static void appendHero(StringBuilder html, TraceReportDocument document) {
        TraceReportEnvironment environment = document.environment();
        TraceSessionSummary summary = document.summary();
        html.append("<header class=\"hero\"><h1>EventLens Trace Report</h1><p class=\"subtitle\">Session ")
                .append(escape(summary.sessionId()))
                .append(" · ")
                .append(escape(simpleName(summary.eventClassName())))
                .append("</p><p class=\"subtitle\">Generated ")
                .append(escape(ReportFormatting.formatGeneratedAt(environment.generatedAtMillis())))
                .append("</p><div class=\"badges\"><span class=\"badge\">")
                .append(escape(document.redactionMode().name()))
                .append("</span><span class=\"badge\">")
                .append(escape(summary.state().name()))
                .append("</span><span class=\"badge ok\">")
                .append(summary.capturedEvents())
                .append(" captured</span></div></header>");

        html.append("<section class=\"card\"><h2>Environment</h2><table><tbody>");
        row(html, "Server", environment.serverVersion());
        row(html, "Paper", environment.paperVersion());
        row(html, "Java", environment.javaVersion());
        row(html, "EventLens", environment.eventLensVersion());
        row(html, "Platform", environment.platformLabel());
        html.append(TABLE_END);

        if (!environment.pluginVersions().isEmpty()) {
            html.append(
                    "<h3>Plugin versions</h3><table><thead><tr><th>Plugin</th><th>Version</th></tr></thead><tbody>");
            for (Map.Entry<String, String> entry : environment.pluginVersions().entrySet()) {
                html.append("<tr><td>")
                        .append(escape(entry.getKey()))
                        .append(CELL_DIVIDER)
                        .append(escape(entry.getValue()))
                        .append(ROW_END);
            }
            html.append(TABLE_END);
        }
        html.append(SECTION_END);
    }

    private static void appendWarnings(StringBuilder html, List<String> warnings) {
        if (warnings.isEmpty()) {
            return;
        }
        html.append("<section class=\"card\"><h2>Warnings</h2><div class=\"warn-list\">");
        for (String warning : warnings) {
            html.append("<div class=\"warn\">").append(escape(warning)).append("</div>");
        }
        html.append("</div></section>");
    }

    private static void appendSession(StringBuilder html, TraceReportDocument document) {
        TraceSessionSummary summary = document.summary();
        html.append("<section class=\"card\"><h2>Session</h2><table><tbody>");
        row(html, "Event", summary.eventClassName());
        row(html, "State", summary.state().name());
        row(html, "Owner", summary.ownerName());
        row(html, "Duration", (summary.lastActivityAtMillis() - summary.startedAtMillis()) + " ms");
        row(html, "Filters", TraceFilterFormatter.describe(document.filter()));
        row(html, "Captured", Integer.toString(summary.capturedEvents()));
        row(html, "Dropped", Integer.toString(summary.droppedEvents()));
        row(html, "Sampled out", Integer.toString(summary.sampledOutEvents()));
        row(html, "Conflicts", summary.conflictSummary().likelyConflictSummary());
        html.append(TABLE_END).append(SECTION_END);
    }

    private static void appendDispatches(StringBuilder html, List<TraceDispatchRecord> dispatches) {
        html.append("<section class=\"card\"><h2>Dispatches</h2>");
        if (dispatches.isEmpty()) {
            html.append("<p>No captured dispatches.</p></section>");
            return;
        }
        for (TraceDispatchRecord dispatch : dispatches) {
            html.append("<h3>#")
                    .append(dispatch.sequence())
                    .append(" · ")
                    .append(escape(DurationStats.formatMillis(dispatch.durationNanos())))
                    .append("</h3><ul class=\"dispatch-meta\">");
            dispatch.playerName()
                    .ifPresent(player ->
                            html.append("<li>Player: ").append(escape(player)).append(LIST_ITEM_END));
            dispatch.worldName()
                    .ifPresent(world ->
                            html.append("<li>World: ").append(escape(world)).append(LIST_ITEM_END));
            if (!dispatch.partialReasons().isEmpty()) {
                html.append("<li>Partial: ")
                        .append(escape(ReportFormatting.formatPartialReasons(dispatch.partialReasons())))
                        .append(LIST_ITEM_END);
            }
            html.append("</ul><table><thead><tr><th>Priority</th><th>Plugin</th><th>Listener</th></tr></thead><tbody>");
            for (TraceListenerSnapshot listener : dispatch.listenerChain()) {
                html.append("<tr><td>")
                        .append(escape(listener.priority()))
                        .append(CELL_DIVIDER)
                        .append(escape(listener.pluginName()))
                        .append(CELL_DIVIDER)
                        .append("<span class=\"mono\">")
                        .append(escape(ListenerDisplayFormatter.format(
                                listener.pluginName(), listener.listenerClassName(), listener.methodName())))
                        .append("</span>")
                        .append(ROW_END);
            }
            html.append(TABLE_END);
        }
        html.append(SECTION_END);
    }

    private static void row(StringBuilder html, String label, String value) {
        html.append("<tr><th>")
                .append(escape(label))
                .append("</th><td>")
                .append(escape(value))
                .append(ROW_END);
    }

    private static String simpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
