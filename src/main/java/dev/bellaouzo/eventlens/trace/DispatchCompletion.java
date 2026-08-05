package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.EventFilterContext;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record DispatchCompletion(
        EventFilterContext endContext,
        long endMillis,
        long endNanos,
        long eventLensOverheadNanos,
        boolean synchronousDispatch,
        List<TraceListenerSnapshot> listenerChain,
        EventSnapshot snapshotBefore,
        EventSnapshot snapshotAfter,
        List<EventSnapshot> priorityCheckpoints,
        List<ListenerTimingRecord> listenerTimings,
        Set<TracePartialReason> partialReasons) {

    public DispatchCompletion(
            EventFilterContext endContext,
            long endMillis,
            List<TraceListenerSnapshot> listenerChain,
            EventSnapshot snapshotBefore,
            EventSnapshot snapshotAfter,
            List<EventSnapshot> priorityCheckpoints) {
        this(
                endContext,
                endMillis,
                0L,
                0L,
                true,
                listenerChain,
                snapshotBefore,
                snapshotAfter,
                priorityCheckpoints,
                List.of(),
                EnumSet.noneOf(TracePartialReason.class));
    }
}
