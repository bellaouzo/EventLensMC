package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.diff.BandChange;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiff;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiffEngine;
import dev.bellaouzo.eventlens.domain.diff.TraceDispatchWarning;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TraceDispatchAnalyzer {

    private static final int EXPECTED_PRIORITY_CHECKPOINTS = 6;

    private TraceDispatchAnalyzer() {}

    public static SnapshotDiff overallDiff(TraceDispatchRecord dispatchRecord, boolean includeUnchanged) {
        return SnapshotDiffEngine.diff(
                dispatchRecord.snapshotBefore(), dispatchRecord.snapshotAfter(), includeUnchanged);
    }

    public static List<BandChange> bandChanges(TraceDispatchRecord dispatchRecord, boolean includeUnchanged) {
        return SnapshotDiffEngine.computeBandChanges(
                dispatchRecord.priorityCheckpoints(), dispatchRecord.listenerChain(), includeUnchanged);
    }

    public static List<TraceDispatchWarning> warnings(TraceDispatchRecord dispatchRecord) {
        List<TraceDispatchWarning> warnings = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        addWarning(
                warnings,
                seen,
                "EARLIEST_CHECKPOINT",
                "before snapshot is at LOWEST checkpoint; earlier listeners may have already run");

        if (dispatchRecord.priorityCheckpoints().size() < EXPECTED_PRIORITY_CHECKPOINTS) {
            addWarning(
                    warnings,
                    seen,
                    "INCOMPLETE_CHECKPOINTS",
                    "expected " + EXPECTED_PRIORITY_CHECKPOINTS + " priority checkpoints but captured "
                            + dispatchRecord.priorityCheckpoints().size());
        }

        collectSnapshotFieldWarnings(warnings, seen, dispatchRecord.snapshotBefore());
        collectSnapshotFieldWarnings(warnings, seen, dispatchRecord.snapshotAfter());

        SnapshotDiff overallDiff = overallDiff(dispatchRecord, false);
        collectCaptureGapWarnings(warnings, seen, overallDiff);

        for (BandChange bandChange : bandChanges(dispatchRecord, false)) {
            if (bandChange.conflictingAttribution()) {
                addWarning(
                        warnings,
                        seen,
                        "CONFLICTING_ATTRIBUTION",
                        "multiple plugins in " + bandChange.priorityBand() + " ("
                                + String.join(", ", bandChange.attributedPlugins())
                                + ") may have caused the same change");
            }

            if (bandChange.attributedPlugins().isEmpty()
                    && !bandChange.diff().changed().isEmpty()) {
                addWarning(
                        warnings,
                        seen,
                        "UNKNOWN_ATTRIBUTION",
                        "changes during " + bandChange.priorityBand()
                                + " but no listeners registered at that priority");
            }

            bandChange
                    .diff()
                    .cancellationTransition()
                    .filter(transition -> transition.kind() != CancellationTransitionKind.UNCHANGED)
                    .ifPresent(transition -> {
                        if (bandChange.conflictingAttribution()) {
                            addWarning(
                                    warnings,
                                    seen,
                                    "CANCELLATION_CONFLICT",
                                    "cancellation changed during " + bandChange.priorityBand()
                                            + " with ambiguous plugin attribution");
                        }
                    });
        }

        return List.copyOf(warnings);
    }

    public static String formatSnapshotValue(SnapshotValue value) {
        return switch (value) {
            case SnapshotValue.Present(var type, var display) -> typedDisplay(type, display);
            case SnapshotValue.Unsupported(var reason) -> "[unsupported: " + reason + "]";
            case SnapshotValue.Truncated(var display, var reason) -> display + " [truncated: " + reason + "]";
        };
    }

    private static String typedDisplay(String type, String display) {
        if (type.isBlank()) {
            return display;
        }
        return type + ":" + display;
    }

    public static String formatPropertyChange(PropertyChange change) {
        return change.property() + ": " + formatSnapshotValue(change.before()) + " -> "
                + formatSnapshotValue(change.after());
    }

    public static String formatCancellationTransition(SnapshotDiff diff) {
        return diff.cancellationTransition()
                .map(transition -> switch (transition.kind()) {
                    case UNCHANGED -> "unchanged (" + transition.before() + ")";
                    case BECAME_CANCELLED -> "became cancelled";
                    case BECAME_UNCANCELLED -> "became uncancelled";
                })
                .orElse(null);
    }

    public static EventSnapshot snapshotBefore(TraceDispatchRecord dispatchRecord) {
        return dispatchRecord.snapshotBefore();
    }

    public static EventSnapshot snapshotAfter(TraceDispatchRecord dispatchRecord) {
        return dispatchRecord.snapshotAfter();
    }

    private static void collectSnapshotFieldWarnings(
            List<TraceDispatchWarning> warnings, Set<String> seen, EventSnapshot snapshot) {
        for (SnapshotField field : snapshot.fields()) {
            switch (field.value()) {
                case SnapshotValue.Unsupported(var reason) ->
                    addWarning(
                            warnings,
                            seen,
                            "UNSUPPORTED_FIELD",
                            field.name() + " at " + snapshot.checkpoint() + ": " + reason);
                case SnapshotValue.Truncated(var display, var reason) ->
                    addWarning(
                            warnings,
                            seen,
                            "TRUNCATED_FIELD",
                            field.name() + " at " + snapshot.checkpoint() + ": " + reason + " (value=" + display + ")");
                case SnapshotValue.Present(var type, var display) -> typedDisplay(type, display);
            }
        }
    }

    private static void collectCaptureGapWarnings(
            List<TraceDispatchWarning> warnings, Set<String> seen, SnapshotDiff diff) {
        for (PropertyChange change : diff.changed()) {
            if (change.before() instanceof SnapshotValue.Unsupported(var reason)
                    && reason.contains("missing before capture")) {
                addWarning(
                        warnings, seen, "CAPTURE_GAP", change.property() + " was not present in the before snapshot");
            }
            if (change.after() instanceof SnapshotValue.Unsupported(var reason)
                    && reason.contains("missing after capture")) {
                addWarning(warnings, seen, "CAPTURE_GAP", change.property() + " was not present in the after snapshot");
            }
        }
    }

    private static void addWarning(List<TraceDispatchWarning> warnings, Set<String> seen, String code, String message) {
        String key = code + "|" + message;
        if (seen.add(key)) {
            warnings.add(new TraceDispatchWarning(code, message));
        }
    }
}
