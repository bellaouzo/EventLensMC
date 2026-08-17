package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceLimits;
import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import java.util.Map;

final class TraceSessionRestart {

    private TraceSessionRestart() {}

    static TraceRestartResult restart(
            TraceSessionManager manager, Map<String, TraceSession> sessions, String sessionId, long nowMillis) {
        manager.expireSessions(nowMillis);
        TraceSession session = sessions.get(sessionId);
        if (session == null) {
            return new TraceRestartResult.NotFound(sessionId);
        }
        if (session.isOpen()) {
            return new TraceRestartResult.StillOpen(sessionId, session.getState());
        }
        if (sessions.values().stream().filter(TraceSession::isOpen).count() >= TraceLimits.MAX_CONCURRENT_SESSIONS) {
            return new TraceRestartResult.SessionLimit("Concurrent session limit reached.");
        }
        int restartCount = session.getRestartCount() + 1;
        TraceSessionConfig config = session.getConfig();
        String ownerName = session.getOwnerName();
        String eventClassName = session.getEventClassName();
        manager.archiveCurrent(sessionId);
        sessions.remove(sessionId);
        TraceSessionSlots.detach(manager, sessionId);
        TraceSessionSlots.insert(manager, sessionId, config, ownerName, nowMillis, restartCount);
        return new TraceRestartResult.Success(sessionId, sessionId, eventClassName, restartCount);
    }
}
