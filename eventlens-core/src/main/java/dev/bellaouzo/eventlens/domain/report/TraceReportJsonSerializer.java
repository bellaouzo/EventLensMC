package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TraceReportJsonSerializer {

    private static final String DURATION_MILLIS = "durationMillis";

    private TraceReportJsonSerializer() {}

    public static String serialize(TraceReportDocument document) {
        StringBuilder json = new StringBuilder(8192);
        json.append('{');
        fieldString(json, 1, "reportVersion", document.reportVersion());
        comma(json);
        fieldString(json, 1, "redactionMode", document.redactionMode().name());
        comma(json);
        key(json, 1, "environment");
        appendEnvironment(json, document.environment(), 1);
        comma(json);
        key(json, 1, "instrumentation");
        TraceReportJsonSupport.appendInstrumentation(json, document.instrumentation(), 1);
        comma(json);
        key(json, 1, "session");
        appendSession(json, document, 1);
        comma(json);
        key(json, 1, "sessionTimingSummary");
        TraceReportJsonSupport.appendOptionalSessionTimingSummary(json, document.sessionTimingSummary(), 1);
        comma(json);
        key(json, 1, "warnings");
        appendStringList(json, document.warnings(), 1);
        comma(json);
        key(json, 1, "dispatches");
        appendDispatches(json, document.dispatches(), 1);
        indent(json, 0);
        json.append('}').append('\n');
        return json.toString();
    }

    static void appendEnvironment(StringBuilder json, TraceReportEnvironment environment, int depth) {
        json.append('{');
        fieldString(json, depth + 1, "serverVersion", environment.serverVersion());
        comma(json);
        fieldString(json, depth + 1, "javaVersion", environment.javaVersion());
        comma(json);
        fieldString(json, depth + 1, "paperVersion", environment.paperVersion());
        comma(json);
        fieldString(json, depth + 1, "eventLensVersion", environment.eventLensVersion());
        comma(json);
        fieldString(json, depth + 1, "platformLabel", environment.platformLabel());
        comma(json);
        fieldLong(json, depth + 1, "generatedAtMillis", environment.generatedAtMillis());
        comma(json);
        key(json, depth + 1, "pluginVersions");
        appendStringMap(json, environment.pluginVersions(), depth + 1);
        indent(json, depth);
        json.append('}');
    }

    private static void appendSession(StringBuilder json, TraceReportDocument document, int depth) {
        TraceSessionSummary summary = document.summary();
        json.append('{');
        fieldString(json, depth + 1, "sessionId", summary.sessionId());
        comma(json);
        fieldString(json, depth + 1, "eventClassName", summary.eventClassName());
        comma(json);
        fieldString(json, depth + 1, "state", summary.state().name());
        comma(json);
        fieldString(json, depth + 1, "ownerName", summary.ownerName());
        comma(json);
        fieldLong(json, depth + 1, "startedAtMillis", summary.startedAtMillis());
        comma(json);
        fieldLong(json, depth + 1, "lastActivityAtMillis", summary.lastActivityAtMillis());
        comma(json);
        fieldLong(json, depth + 1, DURATION_MILLIS, summary.lastActivityAtMillis() - summary.startedAtMillis());
        comma(json);
        fieldInt(json, depth + 1, "capturedEvents", summary.capturedEvents());
        comma(json);
        fieldInt(json, depth + 1, "droppedEvents", summary.droppedEvents());
        comma(json);
        fieldInt(json, depth + 1, "sampledOutEvents", summary.sampledOutEvents());
        comma(json);
        fieldString(json, depth + 1, "filters", TraceFilterFormatter.describe(document.filter()));
        comma(json);
        appendConflictSummary(json, summary.conflictSummary(), depth + 1);
        indent(json, depth);
        json.append('}');
    }

    private static void appendConflictSummary(StringBuilder json, SessionConflictSummary conflictSummary, int depth) {
        key(json, depth, "conflicts");
        json.append('{');
        fieldString(json, depth + 1, "likelySummary", conflictSummary.likelyConflictSummary());
        comma(json);
        fieldInt(json, depth + 1, "dispatchesWithConflicts", conflictSummary.dispatchesWithConflicts());
        comma(json);
        key(json, depth + 1, "suggestions");
        appendStringList(json, conflictSummary.suggestions(), depth + 1);
        indent(json, depth);
        json.append('}');
    }

    private static void appendDispatches(StringBuilder json, List<TraceDispatchRecord> dispatches, int depth) {
        json.append('[');
        for (int index = 0; index < dispatches.size(); index++) {
            if (index > 0) {
                comma(json);
            }
            indent(json, depth + 1);
            appendDispatch(json, dispatches.get(index), depth + 1);
        }
        if (!dispatches.isEmpty()) {
            indent(json, depth);
        }
        json.append(']');
    }

    static void appendDispatch(StringBuilder json, TraceDispatchRecord dispatch, int depth) {
        json.append('{');
        fieldLong(json, depth + 1, "sequence", dispatch.sequence());
        comma(json);
        fieldLong(json, depth + 1, "startedAtMillis", dispatch.startedAtMillis());
        comma(json);
        fieldLong(json, depth + 1, "startedAtNanos", dispatch.startedAtNanos());
        comma(json);
        fieldLong(json, depth + 1, "durationNanos", dispatch.durationNanos());
        comma(json);
        fieldString(json, depth + 1, DURATION_MILLIS, DurationStats.formatMillis(dispatch.durationNanos()));
        comma(json);
        fieldLong(json, depth + 1, "eventLensOverheadNanos", dispatch.eventLensOverheadNanos());
        comma(json);
        fieldString(
                json,
                depth + 1,
                "eventLensOverheadMillis",
                DurationStats.formatMillis(dispatch.eventLensOverheadNanos()));
        comma(json);
        fieldString(json, depth + 1, "eventClassName", dispatch.eventClassName());
        comma(json);
        fieldBoolean(json, depth + 1, "synchronousDispatch", dispatch.synchronousDispatch());
        comma(json);
        fieldBoolean(json, depth + 1, "cancellable", dispatch.cancellable());
        comma(json);
        fieldBoolean(json, depth + 1, "cancelledAtStart", dispatch.cancelledAtStart());
        comma(json);
        fieldBoolean(json, depth + 1, "cancelledAtEnd", dispatch.cancelledAtEnd());
        comma(json);
        fieldOptionalString(json, depth + 1, "playerName", dispatch.playerName());
        comma(json);
        fieldOptionalString(json, depth + 1, "worldName", dispatch.worldName());
        comma(json);
        fieldOptionalInt(json, depth + 1, "blockX", dispatch.blockX());
        comma(json);
        fieldOptionalInt(json, depth + 1, "blockY", dispatch.blockY());
        comma(json);
        fieldOptionalInt(json, depth + 1, "blockZ", dispatch.blockZ());
        comma(json);
        key(json, depth + 1, "snapshotBefore");
        TraceReportJsonDispatchSupport.appendSnapshot(json, dispatch.snapshotBefore(), depth + 1);
        comma(json);
        key(json, depth + 1, "snapshotAfter");
        TraceReportJsonDispatchSupport.appendSnapshot(json, dispatch.snapshotAfter(), depth + 1);
        comma(json);
        key(json, depth + 1, "priorityCheckpoints");
        TraceReportJsonDispatchSupport.appendSnapshotList(json, dispatch.priorityCheckpoints(), depth + 1);
        comma(json);
        key(json, depth + 1, "partialReasons");
        appendEnumList(json, dispatch.partialReasons(), depth + 1);
        comma(json);
        key(json, depth + 1, "listenerChain");
        TraceReportJsonDispatchSupport.appendListenerChain(json, dispatch.listenerChain(), depth + 1);
        comma(json);
        key(json, depth + 1, "listenerTimings");
        TraceReportJsonDispatchSupport.appendListenerTimings(json, dispatch.listenerTimings(), depth + 1);
        indent(json, depth);
        json.append('}');
    }

    static void appendStringList(StringBuilder json, List<String> values, int depth) {
        json.append('[');
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                comma(json);
            }
            indent(json, depth + 1);
            json.append(JsonEscaper.string(values.get(index)));
        }
        if (!values.isEmpty()) {
            indent(json, depth);
        }
        json.append(']');
    }

    static void appendStringMap(StringBuilder json, Map<String, String> values, int depth) {
        json.append('{');
        int index = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (index++ > 0) {
                comma(json);
            }
            fieldString(json, depth + 1, entry.getKey(), entry.getValue());
        }
        if (!values.isEmpty()) {
            indent(json, depth);
        }
        json.append('}');
    }

    static void appendEnumList(StringBuilder json, Set<TracePartialReason> values, int depth) {
        json.append('[');
        int index = 0;
        for (TracePartialReason value : values) {
            if (index++ > 0) {
                comma(json);
            }
            indent(json, depth + 1);
            json.append(JsonEscaper.string(value.name()));
        }
        if (!values.isEmpty()) {
            indent(json, depth);
        }
        json.append(']');
    }

    static void fieldString(StringBuilder json, int depth, String name, String value) {
        key(json, depth, name);
        json.append(JsonEscaper.string(value));
    }

    static void fieldLong(StringBuilder json, int depth, String name, long value) {
        key(json, depth, name);
        json.append(value);
    }

    static void fieldInt(StringBuilder json, int depth, String name, int value) {
        key(json, depth, name);
        json.append(value);
    }

    static void fieldBoolean(StringBuilder json, int depth, String name, boolean value) {
        key(json, depth, name);
        json.append(value);
    }

    static void fieldOptionalInt(StringBuilder json, int depth, String name, Optional<Integer> value) {
        key(json, depth, name);
        json.append(value.map(String::valueOf).orElse("null"));
    }

    static void fieldOptionalString(StringBuilder json, int depth, String name, Optional<String> value) {
        key(json, depth, name);
        if (value.isPresent()) {
            json.append(JsonEscaper.string(value.get()));
        } else {
            json.append("null");
        }
    }

    static void key(StringBuilder json, int depth, String name) {
        indent(json, depth);
        json.append('"').append(name).append("\": ");
    }

    static void comma(StringBuilder json) {
        json.append(',');
    }

    static void indent(StringBuilder json, int depth) {
        json.append('\n').append("  ".repeat(Math.max(0, depth)));
    }

    static String compactDispatch(TraceDispatchRecord dispatch) {
        StringBuilder json = new StringBuilder(2048);
        appendDispatch(json, dispatch, 0);
        return TraceReportJsonSupport.minifyJson(json.toString());
    }
}
