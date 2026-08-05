package dev.bellaouzo.eventlens.paper.instrumentation;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransition;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiffEngine;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.observability.ListenerObservation;
import java.util.List;
import java.util.Optional;

final class ListenerTimingRecordFactory {

    ListenerTimingRecord fromObservation(ListenerObservation observation, long slowThresholdNanos) {
        boolean exceedsThreshold = observation.durationNanos() >= slowThresholdNanos;
        boolean mainThreadBlocked = observation.mainThread()
                && observation.durationNanos()
                        >= dev.bellaouzo.eventlens.domain.observability.PerformanceBudget.MAIN_THREAD_BLOCK_NANOS;

        Optional<EventSnapshot> before = observation.snapshotBefore().map(CompactSnapshotConverter::toEventSnapshot);
        Optional<EventSnapshot> after = observation.snapshotAfter().map(CompactSnapshotConverter::toEventSnapshot);

        List<PropertyChange> propertyChanges = List.of();
        Optional<CancellationTransition> cancellationTransition = Optional.empty();
        if (before.isPresent() && after.isPresent()) {
            var diff = SnapshotDiffEngine.diff(before.get(), after.get(), false);
            propertyChanges = diff.changed();
            cancellationTransition = diff.cancellationTransition();
        }

        return new ListenerTimingRecord(
                observation.invocationOrder(),
                observation.pluginName(),
                observation.listenerClassName(),
                observation.methodName(),
                observation.priority(),
                observation.durationNanos(),
                observation.mainThread(),
                mainThreadBlocked,
                exceedsThreshold,
                observation.stackTrace(),
                observation.threwException(),
                observation.exceptionType(),
                before,
                after,
                propertyChanges,
                cancellationTransition);
    }
}
