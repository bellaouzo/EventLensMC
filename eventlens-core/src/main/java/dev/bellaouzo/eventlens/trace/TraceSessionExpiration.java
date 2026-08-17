package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceLimits;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import java.util.function.Consumer;

final class TraceSessionExpiration {

    private TraceSessionExpiration() {}

    static void expireActiveSessions(Iterable<TraceSession> sessions, long nowMillis, Consumer<String> onCleared) {
        for (TraceSession session : sessions) {
            if (session.isOpen()) {
                expireIfNeeded(session, nowMillis, onCleared);
            }
        }
    }

    private static void expireIfNeeded(TraceSession session, long nowMillis, Consumer<String> onCleared) {
        if (nowMillis - session.getStartedAtMillis() >= session.getConfig().effectiveMaxDurationMillis()) {
            session.stop(TraceSessionState.EXPIRED, nowMillis);
            onCleared.accept(session.getSessionId());
            return;
        }
        if (nowMillis - session.getLastActivityAtMillis() >= TraceLimits.ABANDONED_SESSION_MILLIS) {
            session.stop(TraceSessionState.ABANDONED, nowMillis);
            onCleared.accept(session.getSessionId());
        }
    }
}
