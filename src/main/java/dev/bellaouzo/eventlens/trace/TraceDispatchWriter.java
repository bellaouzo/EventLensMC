package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceLimits;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

final class TraceDispatchWriter {

    private TraceDispatchWriter() {}

    static TraceDispatchRecord buildRecord(
            TraceSessionConfig config,
            TraceSession.PendingDispatch pending,
            DispatchCompletion completion,
            int currentRecordCount) {
        long durationNanos = pending.startedAtNanos() > 0L && completion.endNanos() > 0L
                ? Math.max(0L, completion.endNanos() - pending.startedAtNanos())
                : Math.max(0L, (completion.endMillis() - pending.startedAtMillis()) * 1_000_000L);

        EnumSet<TracePartialReason> partialReasons = EnumSet.copyOf(completion.partialReasons());
        if (!completion.listenerTimings().isEmpty()) {
            partialReasons.remove(TracePartialReason.AGENT_ABSENT);
        } else {
            partialReasons.add(TracePartialReason.AGENT_ABSENT);
        }

        List<TraceListenerSnapshot> boundedListeners =
                completion.listenerChain().size() > TraceLimits.MAX_LISTENERS_PER_DISPATCH
                        ? completion.listenerChain().subList(0, TraceLimits.MAX_LISTENERS_PER_DISPATCH)
                        : completion.listenerChain();
        if (completion.listenerChain().size() > TraceLimits.MAX_LISTENERS_PER_DISPATCH) {
            partialReasons.add(TracePartialReason.LISTENER_LIMIT);
        }
        if (currentRecordCount + 1 >= config.effectiveMaxEventCount()) {
            partialReasons.add(TracePartialReason.RECORD_LIMIT);
        }

        return new TraceDispatchRecord(
                pending.sequence(),
                pending.startedAtMillis(),
                pending.startedAtNanos(),
                durationNanos,
                completion.eventLensOverheadNanos(),
                config.eventClassName(),
                completion.synchronousDispatch(),
                pending.cancellable(),
                pending.cancelledAtStart(),
                completion.endContext().cancellable() && completion.endContext().cancelled(),
                pending.playerName(),
                pending.worldName(),
                pending.blockX(),
                pending.blockY(),
                pending.blockZ(),
                completion.snapshotBefore(),
                completion.snapshotAfter(),
                List.copyOf(completion.priorityCheckpoints()),
                List.copyOf(boundedListeners),
                List.copyOf(completion.listenerTimings()),
                Set.copyOf(partialReasons));
    }
}
