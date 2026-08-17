package dev.bellaouzo.eventlens.domain.trace;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record TraceDispatchRecord(
        long sequence,
        long startedAtMillis,
        long startedAtNanos,
        long durationNanos,
        long eventLensOverheadNanos,
        String eventClassName,
        boolean synchronousDispatch,
        boolean cancellable,
        boolean cancelledAtStart,
        boolean cancelledAtEnd,
        Optional<String> playerName,
        Optional<String> worldName,
        Optional<Integer> blockX,
        Optional<Integer> blockY,
        Optional<Integer> blockZ,
        EventSnapshot snapshotBefore,
        EventSnapshot snapshotAfter,
        List<EventSnapshot> priorityCheckpoints,
        List<TraceListenerSnapshot> listenerChain,
        List<ListenerTimingRecord> listenerTimings,
        Set<TracePartialReason> partialReasons) {

    public TraceDispatchRecord {
        partialReasons = partialReasons == null ? Set.of() : Set.copyOf(partialReasons);
        listenerTimings = listenerTimings == null ? List.of() : List.copyOf(listenerTimings);
    }
}
