package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;

public final class DashboardLiveUpdateSerializer {

    private static final String DURATION_MILLIS = "durationMillis";

    private DashboardLiveUpdateSerializer() {}

    public static String serializeSessionStarted(TraceSessionSummary summary) {
        StringBuilder json = new StringBuilder(256);
        json.append('{');
        appendSessionFields(json, summary);
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}');
        return json.toString();
    }

    public static String serializeDispatchUpdate(TraceSessionSummary summary, TraceDispatchRecord dispatch) {
        StringBuilder json = new StringBuilder(768);
        json.append('{');
        appendSessionFields(json, summary);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "dispatch");
        appendCompactDispatch(json, dispatch, 1);
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}');
        return json.toString();
    }

    private static void appendSessionFields(StringBuilder json, TraceSessionSummary summary) {
        long durationMillis = Math.max(0L, summary.lastActivityAtMillis() - summary.startedAtMillis());
        TraceReportJsonSerializer.fieldString(json, 1, "sessionId", summary.sessionId());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "eventClassName", summary.eventClassName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "state", summary.state().name());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, 1, "capturedEvents", summary.capturedEvents());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, 1, DURATION_MILLIS, durationMillis);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, 1, "startedAtMillis", summary.startedAtMillis());
    }

    private static void appendCompactDispatch(StringBuilder json, TraceDispatchRecord dispatch, int depth) {
        json.append('{');
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "sequence", dispatch.sequence());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "startedAtMillis", dispatch.startedAtMillis());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "durationNanos", dispatch.durationNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, depth + 1, DURATION_MILLIS, DurationStats.formatMillis(dispatch.durationNanos()));
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(
                json, depth + 1, "eventLensOverheadNanos", dispatch.eventLensOverheadNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "eventClassName", dispatch.eventClassName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "cancelledAtStart", dispatch.cancelledAtStart());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "cancelledAtEnd", dispatch.cancelledAtEnd());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalString(json, depth + 1, "playerName", dispatch.playerName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalString(json, depth + 1, "worldName", dispatch.worldName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalInt(json, depth + 1, "blockX", dispatch.blockX());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalInt(json, depth + 1, "blockY", dispatch.blockY());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalInt(json, depth + 1, "blockZ", dispatch.blockZ());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "listenerChain");
        appendCompactListenerChain(json, dispatch.listenerChain(), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "listenerTimings");
        appendCompactListenerTimings(json, dispatch.listenerTimings(), depth + 1);
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendCompactListenerChain(StringBuilder json, List<TraceListenerSnapshot> chain, int depth) {
        json.append('[');
        for (int index = 0; index < chain.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            var listener = chain.get(index);
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('{');
            TraceReportJsonSerializer.fieldInt(json, depth + 2, "registrationOrder", listener.registrationOrder());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "pluginName", listener.pluginName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "listenerClassName", listener.listenerClassName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "methodName", listener.methodName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "priority", listener.priority());
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        if (!chain.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }

    private static void appendCompactListenerTimings(
            StringBuilder json, List<ListenerTimingRecord> timings, int depth) {
        json.append('[');
        for (int index = 0; index < timings.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            ListenerTimingRecord timing = timings.get(index);
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('{');
            TraceReportJsonSerializer.fieldInt(json, depth + 2, "invocationOrder", timing.invocationOrder());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "pluginName", timing.pluginName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "listenerClassName", timing.listenerClassName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "methodName", timing.methodName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 2, "priority", timing.priority());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldLong(json, depth + 2, "durationNanos", timing.durationNanos());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(
                    json, depth + 2, DURATION_MILLIS, DurationStats.formatMillis(timing.durationNanos()));
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldBoolean(
                    json, depth + 2, "exceedsSlowThreshold", timing.exceedsSlowThreshold());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldBoolean(json, depth + 2, "threwException", timing.threwException());
            TraceReportJsonSerializer.fieldOptionalString(json, depth + 2, "exceptionType", timing.exceptionType());
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        if (!timings.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }
}
