package dev.bellaouzo.eventlens.domain.diff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import java.util.List;
import org.junit.jupiter.api.Test;

class SnapshotDiffEngineTest {

    @Test
    void detectsChangedUnchangedAndCancellation() {
        EventSnapshot before = snapshot(
                "LOWEST",
                field("cancelled", bool(false)),
                field("player.name", string("Steve")),
                field("unsupported.field", unsupported("not supported")));
        EventSnapshot after = snapshot(
                "MONITOR",
                field("cancelled", bool(true)),
                field("player.name", string("Steve")),
                field("unsupported.field", unsupported("not supported")));

        SnapshotDiff diff = SnapshotDiffEngine.diff(before, after, true);

        assertEquals(1, diff.changed().size());
        assertEquals("cancelled", diff.changed().getFirst().property());
        assertEquals(2, diff.unchanged().size());
        assertTrue(diff.cancellationTransition().isPresent());
        assertEquals(
                CancellationTransitionKind.BECAME_CANCELLED,
                diff.cancellationTransition().get().kind());
    }

    @Test
    void detectsConflictingBandAttribution() {
        List<EventSnapshot> checkpoints = List.of(
                snapshot("LOWEST", field("cancelled", bool(false))), snapshot("LOW", field("cancelled", bool(true))));
        List<TraceListenerSnapshot> listeners = List.of(
                new TraceListenerSnapshot(1, "PluginA", "A", "onEvent", "LOW", false),
                new TraceListenerSnapshot(2, "PluginB", "B", "onEvent", "LOW", false));

        List<BandChange> bandChanges = SnapshotDiffEngine.computeBandChanges(checkpoints, listeners, false);

        assertEquals(1, bandChanges.size());
        assertEquals("LOW", bandChanges.getFirst().priorityBand());
        assertTrue(bandChanges.getFirst().conflictingAttribution());
        assertEquals(2, bandChanges.getFirst().attributedPlugins().size());
    }

    private static EventSnapshot snapshot(String checkpoint, SnapshotField... fields) {
        return new EventSnapshot("org.example.TestEvent", checkpoint, 1_000L, List.of(fields));
    }

    private static SnapshotField field(String name, SnapshotValue value) {
        return new SnapshotField(name, value);
    }

    private static SnapshotValue string(String value) {
        return new SnapshotValue.Present("string", value);
    }

    private static SnapshotValue bool(boolean value) {
        return new SnapshotValue.Present("boolean", Boolean.toString(value));
    }

    private static SnapshotValue unsupported(String reason) {
        return new SnapshotValue.Unsupported(reason);
    }
}
