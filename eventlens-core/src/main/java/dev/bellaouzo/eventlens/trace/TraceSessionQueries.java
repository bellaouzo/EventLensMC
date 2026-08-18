package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.observability.PerformanceBudget;
import java.util.Collection;
import java.util.List;

final class TraceSessionQueries {

    private TraceSessionQueries() {}

    static List<String> activeSessionIdsForEvent(Collection<TraceSession> sessions, String eventClassName) {
        return sessions.stream()
                .filter(session -> session.isActive() && session.getConfig().acceptsEvent(eventClassName))
                .map(TraceSession::getSessionId)
                .toList();
    }

    static List<String> activeEventClassNames(Collection<TraceSession> sessions) {
        return sessions.stream()
                .filter(TraceSession::isActive)
                .flatMap(session -> session.getEventClassNames().stream())
                .distinct()
                .toList();
    }

    static boolean throttledCaptureForEvent(Collection<TraceSession> sessions, String eventClassName) {
        for (TraceSession session : sessions) {
            if (session.isActive()
                    && session.getConfig().acceptsEvent(eventClassName)
                    && session.isThrottledCapture()) {
                return true;
            }
        }
        return false;
    }

    static long minSlowThresholdForEvent(Collection<TraceSession> sessions, String eventClassName) {
        return sessions.stream()
                .filter(session -> session.isActive() && session.getConfig().acceptsEvent(eventClassName))
                .mapToLong(session -> session.getConfig().slowThresholdNanos())
                .min()
                .orElse(PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS);
    }
}
