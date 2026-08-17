package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraph;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraphEdge;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraphNode;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardReportEntry;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardServerContext;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardSessionEntry;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardStatusPayload;
import java.util.List;

public final class DashboardJsonSerializer {

    private DashboardJsonSerializer() {}

    public static String serializeSessions(List<DashboardSessionEntry> sessions) {
        StringBuilder json = new StringBuilder(256);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, 1, "type", "sessions");
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "sessions");
        json.append('[');
        for (int index = 0; index < sessions.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            appendSession(json, sessions.get(index), 2);
        }
        if (!sessions.isEmpty()) {
            TraceReportJsonSerializer.indent(json, 1);
        }
        json.append(']');
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}');
        return json.toString();
    }

    public static String serializeReports(List<DashboardReportEntry> reports) {
        StringBuilder json = new StringBuilder(256);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, 1, "type", "reports");
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "reports");
        json.append('[');
        for (int index = 0; index < reports.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            appendReport(json, reports.get(index), 2);
        }
        if (!reports.isEmpty()) {
            TraceReportJsonSerializer.indent(json, 1);
        }
        json.append(']');
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}');
        return json.toString();
    }

    public static String serializeGraph(DashboardGraph graph) {
        StringBuilder json = new StringBuilder(512);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, 1, "type", "graph");
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "title", graph.title());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, 1, "truncated", graph.truncated());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "nodes");
        appendNodes(json, graph.nodes(), 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "edges");
        appendEdges(json, graph.edges(), 1);
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}');
        return json.toString();
    }

    public static String serializeStatus(DashboardStatusPayload status) {
        DashboardServerContext server = status.server();
        StringBuilder json = new StringBuilder(512);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, 1, "type", "status");
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, 1, "dashboardEnabled", status.dashboardEnabled());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, 1, "port", status.port());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "bindAddress", status.bindAddress());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, 1, "agentPresent", status.agentPresent());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, 1, "protocolVersion", status.protocolVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "paperVersion", server.paperVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "eventLensVersion", server.eventLensVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "defaultWorldName", server.defaultWorldName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "defaultGameMode", server.defaultGameMode());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, 1, "onlinePlayers", server.onlinePlayers());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldDouble(json, 1, "tps", server.tps());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, 1, "serverTimeMillis", server.serverTimeMillis());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "activeTraceSessionId", status.activeTraceSessionId());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, 1, "activeTraceStartedAtMillis", status.activeTraceStartedAtMillis());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, 1, "activeTraceCapturedEvents", status.activeTraceCapturedEvents());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "activeTraceEventClassName", status.activeTraceEventClassName());
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}');
        return json.toString();
    }

    private static void appendSession(StringBuilder json, DashboardSessionEntry session, int depth) {
        TraceReportJsonSerializer.indent(json, depth);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, depth + 1, "sessionId", session.sessionId());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "eventClassName", session.eventClassName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "state", session.state());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "capturedEvents", session.capturedEvents());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "startedAtMillis", session.startedAtMillis());
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendReport(StringBuilder json, DashboardReportEntry report, int depth) {
        TraceReportJsonSerializer.indent(json, depth);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, depth + 1, "fileName", report.fileName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "lastModifiedMillis", report.lastModifiedMillis());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "sizeBytes", report.sizeBytes());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "format", report.format());
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendNodes(StringBuilder json, List<DashboardGraphNode> nodes, int depth) {
        json.append('[');
        for (int index = 0; index < nodes.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            DashboardGraphNode node = nodes.get(index);
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('{');
            TraceReportJsonSerializer.fieldString(json, depth + 2, "id", node.id());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "label", node.label());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(
                    json, depth + 2, "kind", node.kind().name());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldInt(json, depth + 2, "weight", node.weight());
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        if (!nodes.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }

    private static void appendEdges(StringBuilder json, List<DashboardGraphEdge> edges, int depth) {
        json.append('[');
        for (int index = 0; index < edges.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            DashboardGraphEdge edge = edges.get(index);
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('{');
            TraceReportJsonSerializer.fieldString(json, depth + 2, "sourceId", edge.sourceId());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "targetId", edge.targetId());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldInt(json, depth + 2, "weight", edge.weight());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "label", edge.label());
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        if (!edges.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }
}
