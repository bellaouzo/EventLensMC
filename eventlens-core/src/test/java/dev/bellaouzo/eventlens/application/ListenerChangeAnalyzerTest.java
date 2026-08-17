package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ListenerChangeAnalyzerTest {

    @Test
    void buildsCancellationTimelineFromListenerTransitions() {
        ListenerTimingRecord canceller = listenerWithCancellation(2, "PluginB", "B", "onEvent", "NORMAL", false, true);
        TraceDispatchRecord dispatch = dispatchWithTimings(
                List.of(listenerWithCancellation(1, "PluginA", "A", "onEvent", "LOW", false, false), canceller));

        var timeline = ListenerChangeAnalyzer.cancellationTimeline(dispatch);

        assertEquals(1, timeline.size());
        assertEquals(2, timeline.getFirst().invocationOrder());
        assertEquals("PluginB", timeline.getFirst().pluginName());
        assertEquals(
                CancellationTransitionKind.BECAME_CANCELLED, timeline.getFirst().kind());
    }

    @Test
    void listsListenersWithPropertyOrCancellationChanges() {
        ListenerTimingRecord changed = listenerWithCancellation(1, "PluginA", "A", "onEvent", "LOW", false, true);
        ListenerTimingRecord unchanged = ListenerTimingRecord.timingOnly(
                2,
                "PluginB",
                "B",
                "onEvent",
                "NORMAL",
                100_000L,
                true,
                false,
                false,
                Optional.empty(),
                false,
                Optional.empty());

        var changedListeners =
                ListenerChangeAnalyzer.listenersWithChanges(dispatchWithTimings(List.of(changed, unchanged)));

        assertEquals(1, changedListeners.size());
        assertEquals("PluginA", changedListeners.getFirst().pluginName());
    }

    @Test
    void diffForListenerReturnsEmptyWhenSnapshotsMissing() {
        ListenerTimingRecord timingOnly = ListenerTimingRecord.timingOnly(
                1,
                "PluginA",
                "A",
                "onEvent",
                "LOW",
                100_000L,
                true,
                false,
                false,
                Optional.empty(),
                false,
                Optional.empty());

        var diff = ListenerChangeAnalyzer.diffForListener(timingOnly, false);

        assertTrue(diff.changed().isEmpty());
        assertTrue(diff.cancellationTransition().isEmpty());
    }

    private static ListenerTimingRecord listenerWithCancellation(
            int order,
            String plugin,
            String listenerClass,
            String method,
            String priority,
            boolean beforeCancelled,
            boolean afterCancelled) {
        EventSnapshot before = snapshot("Lbefore:" + plugin, field("cancelled", bool(beforeCancelled)));
        EventSnapshot after = snapshot("Lafter:" + plugin, field("cancelled", bool(afterCancelled)));
        var diff = dev.bellaouzo.eventlens.domain.diff.SnapshotDiffEngine.diff(before, after, false);
        return new ListenerTimingRecord(
                order,
                plugin,
                listenerClass,
                method,
                priority,
                100_000L,
                true,
                false,
                false,
                Optional.empty(),
                false,
                Optional.empty(),
                Optional.of(before),
                Optional.of(after),
                diff.changed(),
                diff.cancellationTransition());
    }

    private static TraceDispatchRecord dispatchWithTimings(List<ListenerTimingRecord> timings) {
        EventSnapshot snapshot = new EventSnapshot("org.example.TestEvent", "LOWEST", 1_000L, List.of());
        return new TraceDispatchRecord(
                1L,
                1_000L,
                1_000_000_000L,
                3_000_000L,
                50_000L,
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
                snapshot,
                snapshot,
                List.of(snapshot),
                List.of(),
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
}
