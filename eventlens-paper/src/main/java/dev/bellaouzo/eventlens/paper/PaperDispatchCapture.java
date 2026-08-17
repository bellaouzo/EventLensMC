package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.paper.instrumentation.AgentInstrumentationAdapter;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;

final class PaperDispatchCapture {

    private PaperDispatchCapture() {}

    static PendingDispatchCapture createCapture(
            Event event, EventSnapshot snapshot, ListenerRegistryPort listenerRegistryPort) {
        List<TraceListenerSnapshot> listenerChain =
                listenerRegistryPort.getListeners(event.getClass().getName()).stream()
                        .map(listener -> new TraceListenerSnapshot(
                                listener.registrationOrder(),
                                listener.pluginName(),
                                listener.listenerClassName(),
                                listener.methodName(),
                                listener.priority(),
                                listener.ignoreCancelled()))
                        .toList();

        PendingDispatchCapture capture = new PendingDispatchCapture(listenerChain);
        if (snapshot != null) {
            capture.checkpoints.add(snapshot);
        }
        return capture;
    }

    static void beginSessions(
            Event event, long dispatchKey, PendingDispatchCapture capture, TraceSessionManager traceSessionManager) {
        var context = TraceEventMetadataExtractor.extract(
                event,
                capture.listenerChain.stream()
                        .map(TraceListenerSnapshot::pluginName)
                        .distinct()
                        .toList());
        long nowMillis = System.currentTimeMillis();
        long nowNanos = System.nanoTime();

        for (String sessionId :
                traceSessionManager.getActiveSessionIdsForEvent(event.getClass().getName())) {
            traceSessionManager.beginEventDispatch(sessionId, dispatchKey, context, nowMillis, nowNanos);
        }
    }

    static void finishDispatchCapture(
            Event event,
            long dispatchKey,
            EventSnapshot monitorSnapshot,
            PendingDispatchCapture capture,
            TraceSessionManager traceSessionManager,
            InstrumentationPort instrumentationPort,
            AgentInstrumentationAdapter agentAdapter) {
        if (monitorSnapshot != null) {
            capture.checkpoints.add(monitorSnapshot);
        }
        if (capture.checkpoints.isEmpty()) {
            if (agentAdapter != null) {
                agentAdapter.clearDispatch(dispatchKey);
            }
            return;
        }

        var endContext = TraceEventMetadataExtractor.extract(
                event,
                capture.listenerChain.stream()
                        .map(TraceListenerSnapshot::pluginName)
                        .distinct()
                        .toList());
        long nowMillis = System.currentTimeMillis();
        long nowNanos = System.nanoTime();
        long slowThreshold =
                traceSessionManager.minSlowThresholdForEvent(event.getClass().getName());

        var completion = PaperDispatchCompletionFactory.create(new PaperDispatchCompletionFactory.FinishContext(
                endContext,
                nowMillis,
                nowNanos,
                capture,
                event.isAsynchronous(),
                slowThreshold,
                instrumentationPort,
                agentAdapter,
                dispatchKey));

        for (String sessionId :
                traceSessionManager.getActiveSessionIdsForEvent(event.getClass().getName())) {
            traceSessionManager.completeEventDispatch(sessionId, dispatchKey, completion);
        }
    }

    static final class PendingDispatchCapture {

        final List<TraceListenerSnapshot> listenerChain;
        final List<EventSnapshot> checkpoints = new ArrayList<>();
        long eventLensOverheadNanos;

        PendingDispatchCapture(List<TraceListenerSnapshot> listenerChain) {
            this.listenerChain = listenerChain;
        }
    }
}
