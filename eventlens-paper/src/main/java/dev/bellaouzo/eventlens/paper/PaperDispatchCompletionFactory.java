package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.paper.instrumentation.AgentInstrumentationAdapter;
import dev.bellaouzo.eventlens.trace.DispatchCompletion;
import java.util.EnumSet;
import java.util.List;

final class PaperDispatchCompletionFactory {

    private PaperDispatchCompletionFactory() {}

    record FinishContext(
            dev.bellaouzo.eventlens.domain.trace.EventFilterContext endContext,
            long nowMillis,
            long nowNanos,
            PaperDispatchCapture.PendingDispatchCapture capture,
            boolean asynchronous,
            long slowThreshold,
            InstrumentationPort instrumentationPort,
            AgentInstrumentationAdapter agentAdapter,
            long dispatchKey) {}

    static DispatchCompletion create(FinishContext context) {
        EnumSet<TracePartialReason> partialReasons = EnumSet.noneOf(TracePartialReason.class);
        List<ListenerTimingRecord> listenerTimings = List.of();

        if (context.agentAdapter() != null) {
            listenerTimings =
                    context.agentAdapter().consumeListenerTimings(context.dispatchKey(), context.slowThreshold());
            if (!context.instrumentationPort().isProtocolCompatible()) {
                partialReasons.add(TracePartialReason.INCOMPATIBLE_AGENT_PROTOCOL);
            }
            if (!context.instrumentationPort().listenerSnapshotsEnabled()) {
                partialReasons.add(TracePartialReason.LISTENER_SNAPSHOTS_UNAVAILABLE);
            }
        } else if (!context.instrumentationPort().isAgentPresent()) {
            partialReasons.add(TracePartialReason.AGENT_ABSENT);
            partialReasons.add(TracePartialReason.LISTENER_SNAPSHOTS_UNAVAILABLE);
        }

        return new DispatchCompletion(
                context.endContext(),
                context.nowMillis(),
                context.nowNanos(),
                context.capture().eventLensOverheadNanos,
                !context.asynchronous(),
                context.capture().listenerChain,
                context.capture().checkpoints.getFirst(),
                context.capture().checkpoints.getLast(),
                List.copyOf(context.capture().checkpoints),
                listenerTimings,
                partialReasons,
                PaperTickSampler.capture());
    }
}
