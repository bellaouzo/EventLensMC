package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceRegion;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Optional;

public final class TraceReportRedactor {

    private TraceReportRedactor() {}

    public static TraceReportDocument apply(TraceReportDocument document, ExportRedactionMode mode) {
        if (mode == ExportRedactionMode.FULL) {
            return document;
        }
        return new TraceReportDocument(
                document.reportVersion(),
                ExportRedactionMode.SHARE_SAFE,
                document.environment(),
                document.instrumentation(),
                redactSummary(document.summary()),
                document.sessionTimingSummary(),
                redactFilter(document.filter()),
                document.warnings(),
                document.dispatches().stream()
                        .map(TraceReportRedactor::redactDispatch)
                        .toList());
    }

    private static TraceFilter redactFilter(TraceFilter filter) {
        return new TraceFilter(
                filter.pluginName(),
                Optional.empty(),
                Optional.empty(),
                Optional.<TraceRegion>empty(),
                filter.cancellationFilter());
    }

    private static TraceDispatchRecord redactDispatch(TraceDispatchRecord dispatch) {
        return new TraceDispatchRecord(
                dispatch.sequence(),
                dispatch.startedAtMillis(),
                dispatch.startedAtNanos(),
                dispatch.durationNanos(),
                dispatch.eventLensOverheadNanos(),
                dispatch.eventClassName(),
                dispatch.synchronousDispatch(),
                dispatch.cancellable(),
                dispatch.cancelledAtStart(),
                dispatch.cancelledAtEnd(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                redactSnapshot(dispatch.snapshotBefore()),
                redactSnapshot(dispatch.snapshotAfter()),
                dispatch.priorityCheckpoints().stream()
                        .map(TraceReportRedactor::redactSnapshot)
                        .toList(),
                dispatch.listenerChain(),
                dispatch.listenerTimings().stream()
                        .map(TraceReportRedactor::redactTiming)
                        .toList(),
                dispatch.partialReasons());
    }

    private static ListenerTimingRecord redactTiming(ListenerTimingRecord timing) {
        return new ListenerTimingRecord(
                timing.invocationOrder(),
                timing.pluginName(),
                timing.listenerClassName(),
                timing.methodName(),
                timing.priority(),
                timing.durationNanos(),
                timing.mainThread(),
                timing.mainThreadBlocked(),
                timing.exceedsSlowThreshold(),
                Optional.empty(),
                timing.threwException(),
                timing.exceptionType(),
                timing.snapshotBefore().map(TraceReportRedactor::redactSnapshot),
                timing.snapshotAfter().map(TraceReportRedactor::redactSnapshot),
                timing.propertyChanges().stream()
                        .map(TraceReportRedactor::redactPropertyChange)
                        .toList(),
                timing.cancellationTransition());
    }

    private static TraceSessionSummary redactSummary(TraceSessionSummary summary) {
        return new TraceSessionSummary(
                summary.sessionId(),
                summary.eventClassName(),
                summary.state(),
                "redacted",
                summary.startedAtMillis(),
                summary.lastActivityAtMillis(),
                summary.capturedEvents(),
                summary.droppedEvents(),
                summary.sampledOutEvents(),
                summary.maxEventCount(),
                summary.maxDurationMillis(),
                summary.slowThresholdNanos(),
                summary.captureStacks(),
                summary.timingSummary(),
                summary.conflictSummary());
    }

    private static PropertyChange redactPropertyChange(PropertyChange change) {
        return new PropertyChange(change.property(), redactValue(change.before()), redactValue(change.after()));
    }

    private static EventSnapshot redactSnapshot(EventSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new EventSnapshot(
                snapshot.eventClassName(),
                snapshot.checkpoint(),
                snapshot.capturedAtMillis(),
                snapshot.capturedAtNanos(),
                redactFields(snapshot.fields()));
    }

    private static List<SnapshotField> redactFields(List<SnapshotField> fields) {
        return fields.stream()
                .map(field -> new SnapshotField(field.name(), redactValue(field.value())))
                .toList();
    }

    private static SnapshotValue redactValue(SnapshotValue value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case SnapshotValue.Present present -> new SnapshotValue.Present(present.type(), "<redacted>");
            case SnapshotValue.Truncated truncated -> new SnapshotValue.Truncated("<redacted>", truncated.reason());
            case SnapshotValue.Unsupported unsupported -> unsupported;
        };
    }
}
