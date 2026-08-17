package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DispatchViewFilterTest {

    private static final long SLOW_THRESHOLD_NANOS = 1_000_000L;

    @Test
    void dispatchPredicateMatchesExactSequence() {
        DispatchViewFilter filter =
                new DispatchViewFilter.Builder().dispatchSequence(3L).build();

        assertTrue(filter.matches(
                dispatchRecord(3L, "WorldGuard", 200_000L, false, false, List.of()), SLOW_THRESHOLD_NANOS));
        assertFalse(filter.matches(
                dispatchRecord(4L, "WorldGuard", 200_000L, false, false, List.of()), SLOW_THRESHOLD_NANOS));
    }

    @Test
    void pluginPredicateMatchesCaseInsensitivePluginName() {
        DispatchViewFilter filter =
                new DispatchViewFilter.Builder().pluginName("worldguard").build();

        assertTrue(filter.matches(
                dispatchRecord(1L, "WorldGuard", 200_000L, false, false, List.of()), SLOW_THRESHOLD_NANOS));
        assertFalse(filter.matches(
                dispatchRecord(1L, "Essentials", 200_000L, false, false, List.of()), SLOW_THRESHOLD_NANOS));
    }

    @Test
    void changedPredicateMatchesPropertyOrCancellationChanges() {
        DispatchViewFilter filter =
                new DispatchViewFilter.Builder().changedOnly(true).build();

        assertTrue(
                filter.matches(dispatchRecord(1L, "PluginA", 100_000L, true, false, List.of()), SLOW_THRESHOLD_NANOS));
        assertTrue(
                filter.matches(dispatchRecord(1L, "PluginA", 100_000L, false, true, List.of()), SLOW_THRESHOLD_NANOS));
        assertFalse(
                filter.matches(dispatchRecord(1L, "PluginA", 100_000L, false, false, List.of()), SLOW_THRESHOLD_NANOS));
    }

    @Test
    void slowPredicateMatchesSlowDispatchAndSlowListener() {
        DispatchViewFilter filter =
                new DispatchViewFilter.Builder().slowOnly(true).build();

        TraceDispatchRecord slowDispatch = dispatchRecord(1L, "PluginA", 2_000_000L, false, false, List.of());
        TraceDispatchRecord slowListener = dispatchRecord(
                2L,
                "PluginA",
                200_000L,
                false,
                false,
                List.of(ListenerTimingRecord.timingOnly(
                        1,
                        "PluginA",
                        "example.Listener",
                        "onEvent",
                        "NORMAL",
                        1_200_000L,
                        true,
                        false,
                        true,
                        Optional.empty(),
                        false,
                        Optional.empty())));
        TraceDispatchRecord fast = dispatchRecord(3L, "PluginA", 200_000L, false, false, List.of());

        assertTrue(filter.matches(slowDispatch, SLOW_THRESHOLD_NANOS));
        assertTrue(filter.matches(slowListener, SLOW_THRESHOLD_NANOS));
        assertFalse(filter.matches(fast, SLOW_THRESHOLD_NANOS));
    }

    @Test
    void conflictPredicateMatchesDetectedConflicts() {
        DispatchViewFilter filter =
                new DispatchViewFilter.Builder().conflictOnly(true).build();

        TraceDispatchRecord conflictDispatch = dispatchRecord(
                1L,
                "PluginA",
                200_000L,
                false,
                false,
                List.of(ListenerTimingRecord.timingOnly(
                        1,
                        "PluginA",
                        "example.Listener",
                        "onEvent",
                        "NORMAL",
                        200_000L,
                        true,
                        false,
                        false,
                        Optional.empty(),
                        true,
                        Optional.of("java.lang.IllegalStateException"))));
        TraceDispatchRecord noConflictDispatch = dispatchRecord(2L, "PluginA", 200_000L, false, false, List.of());

        assertTrue(filter.matches(conflictDispatch, SLOW_THRESHOLD_NANOS));
        assertFalse(filter.matches(noConflictDispatch, SLOW_THRESHOLD_NANOS));
    }

    private static TraceDispatchRecord dispatchRecord(
            long sequence,
            String pluginName,
            long durationNanos,
            boolean changedProperty,
            boolean changedCancellation,
            List<ListenerTimingRecord> listenerTimings) {
        EventSnapshot before = snapshot(false, "50");
        EventSnapshot after = snapshot(changedCancellation, changedProperty ? "51" : "50");
        return new TraceDispatchRecord(
                sequence,
                1_000L,
                1_000_000L,
                durationNanos,
                50_000L,
                "org.example.TestEvent",
                true,
                true,
                false,
                changedCancellation,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                before,
                after,
                List.of(before, after),
                List.of(new TraceListenerSnapshot(1, pluginName, "example.Listener", "onEvent", "NORMAL", false)),
                listenerTimings,
                Set.of());
    }

    private static EventSnapshot snapshot(boolean cancelled, String healthValue) {
        return new EventSnapshot(
                "org.example.TestEvent",
                "NORMAL",
                1_000L,
                List.of(
                        new SnapshotField(
                                "cancelled", new SnapshotValue.Present("boolean", Boolean.toString(cancelled))),
                        new SnapshotField("health", new SnapshotValue.Present("int", healthValue))));
    }
}
