package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
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
        try {
            String newId = manager.startSession(session.getConfig(), session.getOwnerName(), nowMillis);
            return new TraceRestartResult.Success(newId, sessionId, session.getEventClassName());
        } catch (IllegalStateException ex) {
            return new TraceRestartResult.SessionLimit(ex.getMessage());
        }
    }
}
