package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;

public final class TraceReportNdjsonSerializer {

    private TraceReportNdjsonSerializer() {}

    public static String serialize(TraceReportDocument document) {
        StringBuilder ndjson = new StringBuilder(8192);
        ndjson.append(headerLine(document)).append('\n');
        for (TraceDispatchRecord dispatch : document.dispatches()) {
            ndjson.append(dispatchLine(document, dispatch)).append('\n');
        }
        return ndjson.toString();
    }

    private static String headerLine(TraceReportDocument document) {
        StringBuilder json = new StringBuilder(2048);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, 1, "type", "report");
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, 1, "reportVersion", document.reportVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, 1, "redactionMode", document.redactionMode().name());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "environment");
        TraceReportJsonSerializer.appendEnvironment(json, document.environment(), 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "instrumentation");
        TraceReportJsonSupport.appendInstrumentation(json, document.instrumentation(), 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, 1, "sessionId", document.summary().sessionId());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, 1, "eventClassName", document.summary().eventClassName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "sessionTimingSummary");
        TraceReportJsonSupport.appendOptionalSessionTimingSummary(json, document.sessionTimingSummary(), 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "warnings");
        TraceReportJsonSerializer.appendStringList(json, document.warnings(), 1);
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}');
        return TraceReportJsonSupport.minifyJson(json.toString());
    }

    private static String dispatchLine(TraceReportDocument document, TraceDispatchRecord dispatch) {
        StringBuilder json = new StringBuilder(2048);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, 1, "type", "dispatch");
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, 1, "sessionId", document.summary().sessionId());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, 1, "sequence", dispatch.sequence());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "dispatch");
        json.append(TraceReportJsonSerializer.compactDispatch(dispatch));
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}');
        return TraceReportJsonSupport.minifyJson(json.toString());
    }
}
