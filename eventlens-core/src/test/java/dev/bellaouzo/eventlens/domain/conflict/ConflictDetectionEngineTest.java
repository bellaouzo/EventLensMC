package dev.bellaouzo.eventlens.domain.conflict;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConflictDetectionEngineTest {

    private static final long SLOW_THRESHOLD = 1_000_000L;

    @Test
    void detectsCancellationFight() {
        TraceDispatchRecord dispatch = dispatch(
                List.of(
                        snapshot("LOWEST", field("cancelled", bool(false))),
                        snapshot("LOW", field("cancelled", bool(true))),
                        snapshot("HIGH", field("cancelled", bool(false)))),
                List.of(listener("PluginA", "A", "LOW"), listener("PluginB", "B", "HIGH")),
                List.of());

        List<DispatchConflict> conflicts = ConflictDetectionEngine.detect(dispatch, SLOW_THRESHOLD);

        assertTrue(conflicts.stream()
                .anyMatch(conflict -> conflict.kind() == ConflictKind.CANCELLATION_FIGHT
                        && conflict.message().contains("plugins both wrote cancel")));
    }

    @Test
    void detectsMultiPluginPropertyChange() {
        EventSnapshot before = snapshot("LOWEST", field("amount", number(1)));
        EventSnapshot after = snapshot("LOW", field("amount", number(2)));

        TraceDispatchRecord dispatch = dispatch(
                List.of(before, after),
                List.of(listener("PluginA", "A", "LOW"), listener("PluginB", "B", "LOW")),
                List.of());

        List<DispatchConflict> conflicts = ConflictDetectionEngine.detect(dispatch, SLOW_THRESHOLD);

        assertTrue(
                conflicts.stream().anyMatch(conflict -> conflict.kind() == ConflictKind.MULTI_PLUGIN_PROPERTY_CHANGE));
    }

    @Test
    void detectsPropertyRevert() {
        TraceDispatchRecord dispatch = dispatch(
                List.of(
                        snapshot("LOWEST", field("amount", number(1))),
                        snapshot("LOW", field("amount", number(2))),
                        snapshot("HIGH", field("amount", number(1)))),
                List.of(listener("PluginA", "A", "LOW"), listener("PluginB", "B", "HIGH")),
                List.of());

        List<DispatchConflict> conflicts = ConflictDetectionEngine.detect(dispatch, SLOW_THRESHOLD);

        assertTrue(conflicts.stream().anyMatch(conflict -> conflict.kind() == ConflictKind.PROPERTY_REVERTED));
    }

    @Test
    void detectsPostCancelListener() {
        TraceDispatchRecord dispatch = dispatch(
                List.of(
                        snapshot("LOWEST", field("cancelled", bool(false))),
                        snapshot("LOW", field("cancelled", bool(true))),
                        snapshot("HIGH", field("cancelled", bool(true)))),
                List.of(listener("PluginA", "A", "LOW"), listener("PluginB", "B", "HIGH", false)),
                List.of(timing("PluginB", "B", "onEvent", "HIGH", 100_000L)));

        List<DispatchConflict> conflicts = ConflictDetectionEngine.detect(dispatch, SLOW_THRESHOLD);

        assertTrue(conflicts.stream().anyMatch(conflict -> conflict.kind() == ConflictKind.POST_CANCEL_LISTENER));
    }

    @Test
    void detectsMonitorMutation() {
        TraceDispatchRecord dispatch = dispatch(
                List.of(
                        snapshot("HIGHEST", field("cancelled", bool(false))),
                        snapshot("MONITOR", field("cancelled", bool(true)))),
                List.of(listener("PluginA", "A", "MONITOR")),
                List.of());

        List<DispatchConflict> conflicts = ConflictDetectionEngine.detect(dispatch, SLOW_THRESHOLD);

        assertTrue(conflicts.stream().anyMatch(conflict -> conflict.kind() == ConflictKind.MONITOR_MUTATION));
    }

    @Test
    void detectsListenerException() {
        TraceDispatchRecord dispatch = dispatch(
                List.of(snapshot("LOWEST", field("cancelled", bool(false)))),
                List.of(listener("PluginA", "A", "NORMAL")),
                List.of(timingWithException("PluginA", "A", "onEvent", "NORMAL")));

        List<DispatchConflict> conflicts = ConflictDetectionEngine.detect(dispatch, SLOW_THRESHOLD);

        assertTrue(conflicts.stream().anyMatch(conflict -> conflict.kind() == ConflictKind.LISTENER_EXCEPTION));
    }

    @Test
    void detectsSlowListenerChain() {
        TraceDispatchRecord dispatch = new TraceDispatchRecord(
                1L,
                1_000L,
                1_000_000_000L,
                10_000_000L,
                0L,
                "org.example.TestEvent",
                true,
                false,
                false,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                snapshot("LOWEST"),
                snapshot("MONITOR"),
                List.of(snapshot("LOWEST"), snapshot("MONITOR")),
                List.of(),
                List.of(
                        timing("PluginA", "A", "slow1", "NORMAL", 2_000_000L),
                        timing("PluginB", "B", "slow2", "NORMAL", 2_000_000L),
                        timing("PluginC", "C", "slow3", "NORMAL", 2_000_000L)),
                EnumSet.noneOf(TracePartialReason.class));

        List<DispatchConflict> conflicts = ConflictDetectionEngine.detect(dispatch, SLOW_THRESHOLD);

        assertTrue(conflicts.stream().anyMatch(conflict -> conflict.kind() == ConflictKind.SLOW_LISTENER_CHAIN));
    }

    private static TraceDispatchRecord dispatch(
            List<EventSnapshot> checkpoints, List<TraceListenerSnapshot> chain, List<ListenerTimingRecord> timings) {
        EventSnapshot before = checkpoints.getFirst();
        EventSnapshot after = checkpoints.getLast();
        return new TraceDispatchRecord(
                1L,
                1_000L,
                1_000_000_000L,
                1_000_000L,
                0L,
                "org.example.TestEvent",
                true,
                true,
                false,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                before,
                after,
                checkpoints,
                chain,
                timings,
                EnumSet.noneOf(TracePartialReason.class));
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

    private static TraceListenerSnapshot listener(String plugin, String listenerClass, String priority) {
        return listener(plugin, listenerClass, priority, false);
    }

    private static TraceListenerSnapshot listener(
            String plugin, String listenerClass, String priority, boolean ignoreCancelled) {
        return new TraceListenerSnapshot(
                1, plugin, "com.example." + listenerClass, "onEvent", priority, ignoreCancelled);
    }

    private static ListenerTimingRecord timing(
            String plugin, String listenerClass, String method, String priority, long durationNanos) {
        return ListenerTimingRecord.timingOnly(
                1,
                plugin,
                "com.example." + listenerClass,
                method,
                priority,
                durationNanos,
                true,
                false,
                durationNanos >= SLOW_THRESHOLD,
                Optional.empty(),
                false,
                Optional.empty());
    }

    private static ListenerTimingRecord timingWithException(
            String plugin, String listenerClass, String method, String priority) {
        return ListenerTimingRecord.timingOnly(
                1,
                plugin,
                "com.example." + listenerClass,
                method,
                priority,
                100_000L,
                true,
                false,
                false,
                Optional.empty(),
                true,
                Optional.of("java.lang.RuntimeException"));
    }
}
