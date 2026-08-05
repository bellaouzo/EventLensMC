package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransition;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import java.util.List;
import java.util.Optional;

final class TraceReportJsonDispatchSupport {

    private TraceReportJsonDispatchSupport() {}

    static void appendSnapshot(StringBuilder json, EventSnapshot snapshot, int depth) {
        if (snapshot == null) {
            json.append("null");
            return;
        }
        json.append('{');
        TraceReportJsonSerializer.fieldString(json, depth + 1, "eventClassName", snapshot.eventClassName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "checkpoint", snapshot.checkpoint());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "capturedAtMillis", snapshot.capturedAtMillis());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "capturedAtNanos", snapshot.capturedAtNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "fields");
        appendSnapshotFields(json, snapshot.fields(), depth + 1);
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    static void appendSnapshotList(StringBuilder json, List<EventSnapshot> snapshots, int depth) {
        json.append('[');
        for (int index = 0; index < snapshots.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            TraceReportJsonSerializer.indent(json, depth + 1);
            appendSnapshot(json, snapshots.get(index), depth + 1);
        }
        if (!snapshots.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }

    static void appendListenerTiming(StringBuilder json, ListenerTimingRecord timing, int depth) {
        json.append('{');
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "invocationOrder", timing.invocationOrder());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "pluginName", timing.pluginName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "listenerClassName", timing.listenerClassName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "methodName", timing.methodName());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(json, depth + 1, "priority", timing.priority());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "durationNanos", timing.durationNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, depth + 1, "durationMillis", DurationStats.formatMillis(timing.durationNanos()));
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "mainThread", timing.mainThread());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "mainThreadBlocked", timing.mainThreadBlocked());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "exceedsSlowThreshold", timing.exceedsSlowThreshold());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalString(json, depth + 1, "stackTrace", timing.stackTrace());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "threwException", timing.threwException());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldOptionalString(json, depth + 1, "exceptionType", timing.exceptionType());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "snapshotBefore");
        appendSnapshot(json, timing.snapshotBefore().orElse(null), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "snapshotAfter");
        appendSnapshot(json, timing.snapshotAfter().orElse(null), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "propertyChanges");
        appendPropertyChanges(json, timing.propertyChanges(), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "cancellationTransition");
        appendCancellationTransition(json, timing.cancellationTransition(), depth + 1);
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    static void appendListenerChain(StringBuilder json, List<TraceListenerSnapshot> chain, int depth) {
        json.append('[');
        for (int index = 0; index < chain.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            TraceListenerSnapshot listener = chain.get(index);
            json.append('\n').append("  ".repeat(depth + 1)).append('{');
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

    static void appendListenerTimings(StringBuilder json, List<ListenerTimingRecord> timings, int depth) {
        json.append('[');
        for (int index = 0; index < timings.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            TraceReportJsonSerializer.indent(json, depth + 1);
            appendListenerTiming(json, timings.get(index), depth + 1);
        }
        if (!timings.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }

    private static void appendSnapshotFields(StringBuilder json, List<SnapshotField> fields, int depth) {
        json.append('[');
        for (int index = 0; index < fields.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            SnapshotField field = fields.get(index);
            json.append('\n').append("  ".repeat(depth + 1)).append('{');
            TraceReportJsonSerializer.fieldString(json, depth + 2, "name", field.name());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.key(json, depth + 2, "value");
            appendSnapshotValue(json, field.value(), depth + 2);
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        if (!fields.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }

    private static void appendSnapshotValue(StringBuilder json, SnapshotValue value, int depth) {
        if (value == null) {
            json.append("null");
            return;
        }
        json.append('{');
        switch (value) {
            case SnapshotValue.Present(String type, String display) -> {
                TraceReportJsonSerializer.fieldString(json, depth + 1, "kind", "PRESENT");
                TraceReportJsonSerializer.comma(json);
                TraceReportJsonSerializer.fieldString(json, depth + 1, "type", type);
                TraceReportJsonSerializer.comma(json);
                TraceReportJsonSerializer.fieldString(json, depth + 1, "display", display);
            }
            case SnapshotValue.Unsupported(String reason) -> {
                TraceReportJsonSerializer.fieldString(json, depth + 1, "kind", "UNSUPPORTED");
                TraceReportJsonSerializer.comma(json);
                TraceReportJsonSerializer.fieldString(json, depth + 1, "reason", reason);
            }
            case SnapshotValue.Truncated(String display, String reason) -> {
                TraceReportJsonSerializer.fieldString(json, depth + 1, "kind", "TRUNCATED");
                TraceReportJsonSerializer.comma(json);
                TraceReportJsonSerializer.fieldString(json, depth + 1, "display", display);
                TraceReportJsonSerializer.comma(json);
                TraceReportJsonSerializer.fieldString(json, depth + 1, "reason", reason);
            }
        }
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendPropertyChanges(StringBuilder json, List<PropertyChange> changes, int depth) {
        json.append('[');
        for (int index = 0; index < changes.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            PropertyChange change = changes.get(index);
            json.append('\n').append("  ".repeat(depth + 1)).append('{');
            TraceReportJsonSerializer.fieldString(json, depth + 2, "property", change.property());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.key(json, depth + 2, "before");
            appendSnapshotValue(json, change.before(), depth + 2);
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.key(json, depth + 2, "after");
            appendSnapshotValue(json, change.after(), depth + 2);
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        if (!changes.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }

    private static void appendCancellationTransition(
            StringBuilder json, Optional<CancellationTransition> transition, int depth) {
        if (transition.isEmpty()) {
            json.append("null");
            return;
        }
        CancellationTransition value = transition.get();
        json.append('{');
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "before", value.before());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "after", value.after());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldString(
                json, depth + 1, "kind", value.kind().name());
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }
}
