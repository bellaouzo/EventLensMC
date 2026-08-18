package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.modcommon.ModDispatchSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class EventLensSessionRows {

    enum Kind {
        NOTE,
        DISPATCH,
        TITLE,
        SECTION,
        FIELD,
        HANDLER
    }

    record Row(
            Kind kind,
            String primary,
            String secondary,
            int sequence,
            boolean cancelled,
            String peerSessionId,
            int peerSequence) {
        Row {
            peerSessionId = peerSessionId == null ? "" : peerSessionId;
        }

        Row(Kind kind, String primary, String secondary, int sequence, boolean cancelled) {
            this(kind, primary, secondary, sequence, cancelled, "", -1);
        }

        static Row note(String text) {
            return new Row(Kind.NOTE, text, "", -1, false);
        }

        static Row dispatch(TraceDispatchRecord record) {
            return new Row(
                    Kind.DISPATCH,
                    ModDispatchSummary.listLine(record),
                    "",
                    (int) record.sequence(),
                    record.cancelledAtEnd());
        }

        static Row title(
                String primary,
                String secondary,
                boolean cancelled,
                String peerSessionId,
                int peerSequence) {
            return new Row(Kind.TITLE, primary, secondary, -1, cancelled, peerSessionId, peerSequence);
        }

        static Row section(String label) {
            return new Row(Kind.SECTION, label, "", -1, false);
        }

        static Row field(String name, String value) {
            return new Row(Kind.FIELD, name, value, -1, false);
        }

        static Row handler(String primary, String secondary, boolean cancelled) {
            return new Row(Kind.HANDLER, primary, secondary, -1, cancelled);
        }
    }

    private EventLensSessionRows() {}

    static List<Row> build(EventLensScreen screen) {
        if (screen.dispatchSequence() >= 0) {
            return detail(screen);
        }
        return list(screen);
    }

    private static List<Row> list(EventLensScreen screen) {
        Optional<TraceSessionDetail> detail = screen.coordinator()
                .sessionManager()
                .getSessionDetail(screen.sessionId(), screen.sessionGenerationOption());
        if (detail.isEmpty()) {
            return List.of(Row.note("Session not found: " + screen.sessionId()));
        }
        List<TraceDispatchRecord> records = detail.orElseThrow().records();
        if (records.isEmpty()) {
            return List.of(Row.note("Nothing captured yet. Trigger the event."));
        }
        int from = Math.max(0, records.size() - 64);
        List<Row> rows = new ArrayList<>();
        if (from > 0) {
            rows.add(Row.note("Showing last 64 of " + records.size()));
        }
        for (int i = from; i < records.size(); i++) {
            rows.add(Row.dispatch(records.get(i)));
        }
        return rows;
    }

    private static List<Row> detail(EventLensScreen screen) {
        ModTraceResults.ViewResult result = screen.coordinator()
                .viewDispatch(screen.sessionId(), screen.dispatchSequence(), screen.sessionGenerationOption());
        if (result.records().isEmpty()) {
            return List.of(Row.note(result.message()));
        }
        TraceDispatchRecord record = result.records().getFirst();
        List<Row> rows = new ArrayList<>();
        rows.add(Row.title(
                "Dispatch #" + record.sequence()
                        + (record.correlation().linked()
                                ? "  linked " + record.correlation().peerSessionId().orElse("")
                                : ""),
                String.format(Locale.ROOT, "%.2f ms", record.durationNanos() / 1_000_000.0),
                record.cancelledAtEnd(),
                record.correlation().peerSessionId().orElse(""),
                record.correlation().peerSequence().map(Long::intValue).orElse(-1)));
        rows.add(Row.section("Fields"));
        List<SnapshotField> fields = record.snapshotAfter() == null ? List.of() : record.snapshotAfter().fields();
        if (fields == null || fields.isEmpty()) {
            rows.add(Row.note("No snapshot fields"));
        } else {
            int shown = 0;
            for (SnapshotField field : fields) {
                if (shown++ >= 16) {
                    rows.add(Row.note("…"));
                    break;
                }
                rows.add(Row.field(field.name(), displayValue(field.value())));
            }
        }
        List<ListenerTimingRecord> timings = record.listenerTimings();
        if (timings != null && !timings.isEmpty()) {
            rows.add(Row.section("Handlers"));
            for (ListenerTimingRecord timing : timings) {
                boolean cancelled = timing.cancellationTransition()
                        .filter(transition -> transition.kind() == CancellationTransitionKind.BECAME_CANCELLED)
                        .isPresent();
                rows.add(Row.handler(
                        "#" + timing.invocationOrder() + "  " + timing.pluginName(),
                        simpleName(timing.listenerClassName())
                                + "#"
                                + timing.methodName()
                                + "  "
                                + String.format(Locale.ROOT, "%.2f ms", timing.durationNanos() / 1_000_000.0),
                        cancelled));
            }
        }
        return rows;
    }

    private static String displayValue(SnapshotValue value) {
        return switch (value) {
            case SnapshotValue.Present present -> present.display();
            case SnapshotValue.Truncated truncated -> truncated.display();
            case SnapshotValue.Unsupported unsupported -> "?" + unsupported.reason();
        };
    }

    private static String simpleName(String className) {
        int dot = className.lastIndexOf('.');
        return dot < 0 ? className : className.substring(dot + 1);
    }
}
