package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class TraceSessionDispatchCompleter {

    private TraceSessionDispatchCompleter() {}

    static void complete(
            TraceSession session,
            Map<Long, TraceSession.PendingDispatch> pendingForSession,
            long dispatchKey,
            DispatchCompletion completion,
            DispatchCaptureListener dispatchCaptureListener,
            Runnable afterComplete) {
        TraceSession.PendingDispatch pending = pendingForSession.remove(dispatchKey);
        if (pending == null) {
            return;
        }

        Optional<TraceDispatchRecord> captured = session.completeDispatch(completion, pending);
        if (captured.isPresent() && dispatchCaptureListener != null) {
            dispatchCaptureListener.onDispatchCaptured(session.getSessionId(), captured.get());
        }
        afterComplete.run();
    }

    static Map<Long, TraceSession.PendingDispatch> pendingMap(
            Map<String, Map<Long, TraceSession.PendingDispatch>> pendingDispatches, String sessionId) {
        return pendingDispatches.computeIfAbsent(sessionId, ignored -> new ConcurrentHashMap<>());
    }
}
