package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransition;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiff;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiffEngine;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.CancellationTimelineEntry;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ListenerChangeAnalyzer {

    private ListenerChangeAnalyzer() {}

    public static SnapshotDiff diffForListener(ListenerTimingRecord timing, boolean includeUnchanged) {
        if (timing.snapshotBefore().isEmpty() || timing.snapshotAfter().isEmpty()) {
            return new SnapshotDiff(List.of(), List.of(), Optional.empty());
        }
        EventSnapshot before = timing.snapshotBefore().orElseThrow();
        EventSnapshot after = timing.snapshotAfter().orElseThrow();
        return SnapshotDiffEngine.diff(before, after, includeUnchanged);
    }

    public static List<CancellationTimelineEntry> cancellationTimeline(TraceDispatchRecord dispatch) {
        List<CancellationTimelineEntry> timeline = new ArrayList<>();
        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            timing.cancellationTransition()
                    .filter(transition -> transition.kind() != CancellationTransitionKind.UNCHANGED)
                    .ifPresent(transition -> timeline.add(toTimelineEntry(timing, transition)));
        }
        return List.copyOf(timeline);
    }

    public static List<ListenerTimingRecord> listenersWithChanges(TraceDispatchRecord dispatch) {
        return dispatch.listenerTimings().stream()
                .filter(timing -> !timing.propertyChanges().isEmpty()
                        || timing.cancellationTransition()
                                .filter(transition -> transition.kind() != CancellationTransitionKind.UNCHANGED)
                                .isPresent()
                        || timing.threwException())
                .toList();
    }

    private static CancellationTimelineEntry toTimelineEntry(
            ListenerTimingRecord timing, CancellationTransition transition) {
        return new CancellationTimelineEntry(
                timing.invocationOrder(),
                timing.pluginName(),
                timing.listenerClassName(),
                timing.methodName(),
                transition.kind(),
                transition.before(),
                transition.after());
    }
}
