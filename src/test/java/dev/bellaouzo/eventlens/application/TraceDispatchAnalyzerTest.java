package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraceDispatchAnalyzerTest {

    @Test
    void warningsIncludeUnsupportedConflictingAndCaptureIssues() {
        EventSnapshot before = snapshot(
                "LOWEST",
                field("cancelled", bool(false)),
                field("player.name", unsupported("not supported on this event")));
        EventSnapshot after = snapshot("MONITOR", field("cancelled", bool(true)));

        EventSnapshot low = snapshot("LOW", field("cancelled", bool(true)));

        TraceDispatchRecord dispatchRecord = new TraceDispatchRecord(
                1L,
                1_000L,
                1_000_000_000L,
                1_000_000L,
                0L,
                "org.example.TestEvent",
                true,
                true,
                false,
                true,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                before,
                after,
                List.of(before, low, after),
                List.of(
                        new TraceListenerSnapshot(1, "PluginA", "A", "onEvent", "LOW", false),
                        new TraceListenerSnapshot(2, "PluginB", "B", "onEvent", "LOW", false)),
                List.of(),
                EnumSet.of(TracePartialReason.AGENT_ABSENT));

        var warnings = TraceDispatchAnalyzer.warnings(dispatchRecord);

        assertTrue(warnings.stream().anyMatch(warning -> warning.code().equals("UNSUPPORTED_FIELD")));
        assertTrue(warnings.stream().anyMatch(warning -> warning.code().equals("CONFLICTING_ATTRIBUTION")));
        assertTrue(warnings.stream().anyMatch(warning -> warning.code().equals("INCOMPLETE_CHECKPOINTS")));
        assertTrue(warnings.stream().anyMatch(warning -> warning.code().equals("EARLIEST_CHECKPOINT")));
    }

    private static EventSnapshot snapshot(String checkpoint, SnapshotField... fields) {
        return new EventSnapshot("org.example.TestEvent", checkpoint, 1_000L, List.of(fields));
    }

    private static SnapshotField field(String name, SnapshotValue value) {
        return new SnapshotField(name, value);
    }

    private static SnapshotValue bool(boolean value) {
        return new SnapshotValue.Present("boolean", Boolean.toString(value));
    }

    private static SnapshotValue unsupported(String reason) {
        return new SnapshotValue.Unsupported(reason);
    }
}
