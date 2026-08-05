package dev.bellaouzo.eventlens.domain.conflict;

import dev.bellaouzo.eventlens.domain.diff.BandChange;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiff;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiffEngine;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class SnapshotConflictRules {

    private SnapshotConflictRules() {}

    static List<DispatchConflict> detectCancellationFight(long sequence, List<BandChange> bandChanges) {
        List<CancellationTransitionKind> transitions = new ArrayList<>();
        List<String> involvedBands = new ArrayList<>();
        Set<String> plugins = new LinkedHashSet<>();

        for (BandChange bandChange : bandChanges) {
            bandChange
                    .diff()
                    .cancellationTransition()
                    .filter(transition -> transition.kind() != CancellationTransitionKind.UNCHANGED)
                    .ifPresent(transition -> {
                        transitions.add(transition.kind());
                        involvedBands.add(bandChange.priorityBand());
                        plugins.addAll(bandChange.attributedPlugins());
                    });
        }

        if (transitions.size() < 2 && !transitions.contains(CancellationTransitionKind.BECAME_UNCANCELLED)) {
            return List.of();
        }

        String message = "Cancellation toggled across " + String.join(" → ", involvedBands) + " (" + transitions.size()
                + " transitions)";
        return List.of(new DispatchConflict(
                ConflictKind.CANCELLATION_FIGHT,
                ConflictSeverity.HIGH,
                message,
                List.copyOf(plugins),
                Optional.of(sequence)));
    }

    static List<DispatchConflict> detectMultiPluginChanges(long sequence, List<BandChange> bandChanges) {
        List<DispatchConflict> conflicts = new ArrayList<>();
        for (BandChange bandChange : bandChanges) {
            if (bandChange.conflictingAttribution()) {
                List<String> changedProperties = bandChange.diff().changed().stream()
                        .map(PropertyChange::property)
                        .filter(property -> !"cancelled".equals(property))
                        .toList();
                if (!changedProperties.isEmpty()) {
                    conflicts.add(new DispatchConflict(
                            ConflictKind.MULTI_PLUGIN_PROPERTY_CHANGE,
                            ConflictSeverity.HIGH,
                            "Properties " + String.join(", ", changedProperties) + " changed during "
                                    + bandChange.priorityBand() + " with multiple plugins ("
                                    + String.join(", ", bandChange.attributedPlugins()) + ")",
                            bandChange.attributedPlugins(),
                            Optional.of(sequence)));
                }
            }
        }
        return conflicts;
    }

    static List<DispatchConflict> detectPropertyReverts(long sequence, List<EventSnapshot> checkpoints) {
        if (checkpoints.size() < 2) {
            return List.of();
        }

        Map<String, SnapshotValue> originalValues = ConflictDetectionSupport.valueMap(checkpoints.getFirst());
        List<DispatchConflict> conflicts = new ArrayList<>();

        for (int index = 1; index < checkpoints.size(); index++) {
            EventSnapshot previous = checkpoints.get(index - 1);
            EventSnapshot current = checkpoints.get(index);
            SnapshotDiff diff = SnapshotDiffEngine.diff(previous, current, false);
            for (PropertyChange change : diff.changed()) {
                if ("cancelled".equals(change.property())) {
                    continue;
                }
                SnapshotValue original = originalValues.get(change.property());
                SnapshotValue after = change.after();
                if (original != null
                        && ConflictDetectionSupport.valuesEqual(original, after)
                        && !ConflictDetectionSupport.valuesEqual(change.before(), after)) {
                    conflicts.add(new DispatchConflict(
                            ConflictKind.PROPERTY_REVERTED,
                            ConflictSeverity.MEDIUM,
                            change.property() + " was reverted to its original value during " + current.checkpoint(),
                            List.of(),
                            Optional.of(sequence)));
                }
            }
        }

        return conflicts;
    }

    static List<DispatchConflict> detectMonitorMutations(long sequence, List<BandChange> bandChanges) {
        return bandChanges.stream()
                .filter(bandChange -> "MONITOR".equalsIgnoreCase(bandChange.priorityBand()))
                .filter(bandChange -> !bandChange.diff().changed().isEmpty()
                        || bandChange
                                .diff()
                                .cancellationTransition()
                                .map(transition -> transition.kind() != CancellationTransitionKind.UNCHANGED)
                                .orElse(false))
                .map(bandChange -> new DispatchConflict(
                        ConflictKind.MONITOR_MUTATION,
                        ConflictSeverity.MEDIUM,
                        "Event properties or cancellation changed at MONITOR (listeners should observe only)",
                        bandChange.attributedPlugins(),
                        Optional.of(sequence)))
                .toList();
    }
}
