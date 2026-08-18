package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Optional;

public final class TraceReportViewerJsonSerializer {

    private static final String HOOK_CLASS = "PaperTraceHookManager";
    private static final String DURATION_MILLIS = "durationMillis";

    private TraceReportViewerJsonSerializer() {}

    public static String serialize(TraceReportDocument document) {
        StringBuilder json = new StringBuilder(2048);
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, 1, "reportVersion", document.reportVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, 1, "redactionMode", document.redactionMode().name());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "environment");
        appendEnvironment(json, document.environment(), 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "instrumentation");
        appendInstrumentation(json, document.instrumentation(), 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "session");
        appendSession(json, document, 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "warnings");
        TraceReportJsonSerializer.appendStringList(json, document.warnings(), 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, 1, "dispatches");
        appendDispatches(json, document.dispatches(), 1);
        TraceReportJsonSerializer.indent(json, 0);
        json.append('}').append('\n');
        return json.toString();
    }

    private static void appendEnvironment(StringBuilder json, TraceReportEnvironment environment, int depth) {
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, depth + 1, "serverVersion", environment.serverVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "paperVersion", environment.paperVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "eventLensVersion", environment.eventLensVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "platformLabel", environment.platformLabel());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, depth + 1, "runtimeKind", environment.runtimeKind().wireName());
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendInstrumentation(
            StringBuilder json, TraceReportInstrumentation instrumentation, int depth) {
        json.append('{');
        TraceReportJsonSerializer.fieldString(
                json, depth + 1, "mode", instrumentation.mode().name());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "agentPresent", instrumentation.agentPresent());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "protocolVersion", instrumentation.protocolVersion());
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendSession(StringBuilder json, TraceReportDocument document, int depth) {
        TraceSessionSummary summary = document.summary();
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, depth + 1, "sessionId", summary.sessionId());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "eventClassName", summary.eventClassName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, depth + 1, "state", summary.state().name());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "ownerName", summary.ownerName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "startedAtMillis", summary.startedAtMillis());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(
                json, depth + 1, DURATION_MILLIS, summary.lastActivityAtMillis() - summary.startedAtMillis());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "capturedEvents", summary.capturedEvents());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "droppedEvents", summary.droppedEvents());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "sampledOutEvents", summary.sampledOutEvents());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, depth + 1, "filters", TraceFilterFormatter.describe(document.filter()));
        document.sessionTimingSummary().ifPresent(timing -> {
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.key(json, depth + 1, "dispatchStats");
            appendDurationStats(json, timing.dispatchStats(), depth + 1);
        });
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendDurationStats(StringBuilder json, DurationStats stats, int depth) {
        json.append('{');
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "count", stats.count());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "minNanos", stats.minNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "maxNanos", stats.maxNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "averageNanos", stats.averageNanos());
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendDispatches(StringBuilder json, List<TraceDispatchRecord> dispatches, int depth) {
        json.append('[');
        for (int index = 0; index < dispatches.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            TraceReportJsonSerializer.indent(json, depth + 1);
            appendDispatch(json, dispatches.get(index), depth + 1);
        }
        if (!dispatches.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }

    private static void appendDispatch(StringBuilder json, TraceDispatchRecord dispatch, int depth) {
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
        TraceReportJsonSerializer.fieldOptionalString(
                json,
                depth + 1,
                "playerName",
                firstPresent(dispatch.playerName(), displayField(dispatch, "player.name")));
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalString(json, depth + 1, "worldName", dispatch.worldName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalString(
                json, depth + 1, "blockMaterial", displayField(dispatch, "block.type"));
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalInt(json, depth + 1, "blockX", dispatch.blockX());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalInt(json, depth + 1, "blockY", dispatch.blockY());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalInt(json, depth + 1, "blockZ", dispatch.blockZ());
        dispatch.correlation().correlationKey().ifPresent(key -> {
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 1, "correlationKey", key);
        });
        dispatch.correlation().actionKind().ifPresent(kind -> {
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(json, depth + 1, "actionKind", kind);
        });
        dispatch.ticks().serverTick().ifPresent(tick -> {
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldLong(json, depth + 1, "serverTick", tick);
        });
        dispatch.ticks().msptMillis().ifPresent(mspt -> {
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldDouble(json, depth + 1, "msptMillis", mspt);
        });
        appendListeners(json, dispatch, depth);
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendListeners(StringBuilder json, TraceDispatchRecord dispatch, int depth) {
        List<ListenerTimingRecord> timings = dispatch.listenerTimings();
        if (!timings.isEmpty()) {
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.key(json, depth + 1, "listenerTimings");
            appendTimings(json, timings, depth + 1);
            return;
        }
        List<TraceListenerSnapshot> chain = dispatch.listenerChain().stream()
                .filter(listener -> !listener.listenerClassName().contains(HOOK_CLASS))
                .toList();
        if (chain.isEmpty()) {
            return;
        }
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "listenerChain");
        TraceReportJsonDispatchSupport.appendListenerChain(json, chain, depth + 1);
    }

    private static void appendTimings(StringBuilder json, List<ListenerTimingRecord> timings, int depth) {
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
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        TraceReportJsonSerializer.indent(json, depth);
        json.append(']');
    }

    private static Optional<String> firstPresent(Optional<String> primary, Optional<String> fallback) {
        return primary.isPresent() ? primary : fallback;
    }

    private static Optional<String> displayField(TraceDispatchRecord dispatch, String name) {
        return displayField(dispatch.snapshotBefore(), name).or(() -> displayField(dispatch.snapshotAfter(), name));
    }

    private static Optional<String> displayField(EventSnapshot snapshot, String name) {
        if (snapshot == null || snapshot.fields() == null) {
            return Optional.empty();
        }
        for (SnapshotField field : snapshot.fields()) {
            if (name.equals(field.name()) && field.value() instanceof SnapshotValue.Present present) {
                return Optional.of(present.display());
            }
        }
        return Optional.empty();
    }
}
