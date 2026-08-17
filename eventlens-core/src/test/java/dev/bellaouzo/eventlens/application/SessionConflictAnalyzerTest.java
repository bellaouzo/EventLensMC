package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.conflict.ConflictKind;
import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
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

class SessionConflictAnalyzerTest {

    @Test
    void buildsLikelySummaryAndInvestigationTargets() {
        TraceDispatchRecord dispatch = new TraceDispatchRecord(
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
                snapshot("LOWEST", field("cancelled", bool(false)), field("amount", number(1))),
                snapshot("MONITOR", field("cancelled", bool(true)), field("amount", number(2))),
                List.of(
                        snapshot("LOWEST", field("cancelled", bool(false)), field("amount", number(1))),
                        snapshot("LOW", field("cancelled", bool(true)), field("amount", number(2))),
                        snapshot("MONITOR", field("cancelled", bool(true)), field("amount", number(2)))),
                List.of(
                        new TraceListenerSnapshot(1, "PluginA", "com.example.A", "onEvent", "LOW", false),
                        new TraceListenerSnapshot(2, "PluginB", "com.example.B", "onEvent", "LOW", false)),
                List.of(),
                EnumSet.of(TracePartialReason.AGENT_ABSENT));

        SessionConflictSummary summary = SessionConflictAnalyzer.analyze(List.of(dispatch), 1_000_000L);

        assertTrue(summary.dispatchesWithConflicts() > 0);
        assertFalse(summary.likelyConflictSummary().contains("No conflicts"));
        assertTrue(summary.countsByKind().containsKey(ConflictKind.CANCELLATION_FIGHT)
                || summary.countsByKind().containsKey(ConflictKind.MULTI_PLUGIN_PROPERTY_CHANGE));
        assertFalse(summary.investigationTargets().isEmpty());
        assertFalse(summary.suggestions().isEmpty());
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

    private static SnapshotValue number(int value) {
        return new SnapshotValue.Present("int", Integer.toString(value));
    }
}
