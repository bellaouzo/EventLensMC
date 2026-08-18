package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.trace.EventFilterContext;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.trace.DispatchCompletion;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

final class ModDispatchCompletions {

    private ModDispatchCompletions() {}

    static EventFilterContext context(String eventClassName, Optional<String> playerName, Optional<String> worldName) {
        return new EventFilterContext(
                eventClassName,
                false,
                false,
                playerName,
                worldName,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of());
    }

    static DispatchCompletion completion(
            EventFilterContext context,
            long endMillis,
            long endNanos,
            EventSnapshot snapshot,
            List<ListenerTimingRecord> timings,
            boolean agentPresent,
            boolean snapshotsEnabled) {
        EnumSet<TracePartialReason> reasons = EnumSet.noneOf(TracePartialReason.class);
        if (!agentPresent) {
            reasons.add(TracePartialReason.AGENT_ABSENT);
            reasons.add(TracePartialReason.LISTENER_SNAPSHOTS_UNAVAILABLE);
        } else if (!snapshotsEnabled) {
            reasons.add(TracePartialReason.LISTENER_SNAPSHOTS_UNAVAILABLE);
        }
        return new DispatchCompletion(
                context,
                endMillis,
                endNanos,
                0L,
                true,
                List.of(),
                snapshot,
                snapshot,
                List.of(),
                List.copyOf(timings),
                reasons,
                dev.bellaouzo.eventlens.domain.trace.DispatchTickContext.client(endMillis / 50L));
    }

    static EventSnapshot snapshot(String eventClassName, long endMillis, long endNanos, List<SnapshotField> fields) {
        return new EventSnapshot(eventClassName, "DISPATCH", endMillis, endNanos, fields);
    }

    static List<ListenerTimingRecord> renumber(List<ListenerTimingRecord> timings) {
        List<ListenerTimingRecord> numbered = new ArrayList<>(timings.size());
        int order = 1;
        for (ListenerTimingRecord timing : timings) {
            numbered.add(new ListenerTimingRecord(
                    order++,
                    timing.pluginName(),
                    timing.listenerClassName(),
                    timing.methodName(),
                    timing.priority(),
                    timing.durationNanos(),
                    timing.mainThread(),
                    timing.mainThreadBlocked(),
                    timing.exceedsSlowThreshold(),
                    timing.stackTrace(),
                    timing.threwException(),
                    timing.exceptionType(),
                    timing.snapshotBefore(),
                    timing.snapshotAfter(),
                    timing.propertyChanges(),
                    timing.cancellationTransition()));
        }
        return numbered;
    }
}
