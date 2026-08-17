package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import java.util.concurrent.ConcurrentHashMap;

final class TraceSessionSlots {

    private TraceSessionSlots() {}

    static String insert(
            TraceSessionManager manager,
            String sessionId,
            TraceSessionConfig config,
            String ownerName,
            long nowMillis,
            int restartCount) {
        manager.sessions().put(sessionId, new TraceSession(sessionId, config, ownerName, nowMillis, restartCount));
        manager.pendingDispatches().put(sessionId, new ConcurrentHashMap<>());
        manager.refreshAfterMutation();
        manager.notifyLifecycle(sessionId, true);
        return sessionId;
    }

    static void detach(TraceSessionManager manager, String sessionId) {
        manager.clearPending(sessionId);
        manager.notifyLifecycle(sessionId, false);
    }
}
