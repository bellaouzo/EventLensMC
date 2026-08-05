package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.trace.EventFilterContext;
import dev.bellaouzo.eventlens.domain.trace.TraceLimits;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionExportBundle;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TraceSessionManager {

    private final Map<String, TraceSession> sessions = new LinkedHashMap<>();
    private final Map<String, Map<Long, TraceSession.PendingDispatch>> pendingDispatches = new ConcurrentHashMap<>();
    private InstrumentationPort instrumentationPort;
    private DispatchCaptureListener dispatchCaptureListener;

    public void setDispatchCaptureListener(DispatchCaptureListener dispatchCaptureListener) {
        this.dispatchCaptureListener = dispatchCaptureListener;
    }

    public void setInstrumentationPort(InstrumentationPort instrumentationPort) {
        this.instrumentationPort = instrumentationPort;
    }

    public synchronized String startSession(TraceSessionConfig config, String ownerName, long nowMillis) {
        expireSessions(nowMillis);

        long activeCount =
                sessions.values().stream().filter(TraceSession::isActive).count();
        if (activeCount >= TraceLimits.MAX_CONCURRENT_SESSIONS) {
            throw new IllegalStateException("Concurrent session limit reached.");
        }

        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        TraceSession session = new TraceSession(sessionId, config, ownerName, nowMillis);
        sessions.put(sessionId, session);
        pendingDispatches.put(sessionId, new ConcurrentHashMap<>());
        refreshInstrumentationState();
        return sessionId;
    }

    public synchronized List<TraceSessionSummary> listSessions() {
        return sessions.values().stream()
                .map(session -> session.toSummary(TraceSessionInstrumentation.isAgentPresent(instrumentationPort)))
                .toList();
    }

    public synchronized Optional<TraceSessionExportBundle> getExportBundle(String sessionId) {
        TraceSession session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(new TraceSessionExportBundle(
                session.toSummary(TraceSessionInstrumentation.isAgentPresent(instrumentationPort)),
                session.getConfig(),
                session.getRecordsSnapshot()));
    }

    public synchronized Optional<TraceSessionConfig> getSessionConfig(String sessionId) {
        TraceSession session = sessions.get(sessionId);
        return session == null ? Optional.empty() : Optional.of(session.getConfig());
    }

    public synchronized Optional<TraceSessionDetail> getSessionDetail(String sessionId) {
        TraceSession session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(new TraceSessionDetail(
                session.toSummary(TraceSessionInstrumentation.isAgentPresent(instrumentationPort)),
                session.getRecordsSnapshot()));
    }

    public synchronized List<String> stopSessionsForOwner(String ownerName, long nowMillis) {
        List<String> stopped = new ArrayList<>();
        for (TraceSession session : sessions.values()) {
            if (session.isActive() && session.getOwnerName().equalsIgnoreCase(ownerName)) {
                session.stop(TraceSessionState.STOPPED, nowMillis);
                stopped.add(session.getSessionId());
                clearPending(session.getSessionId());
                notifySessionStopped(session.getSessionId());
            }
        }
        refreshInstrumentationState();
        return stopped;
    }

    public synchronized Optional<String> stopSession(String sessionId, long nowMillis) {
        TraceSession session = sessions.get(sessionId);
        if (session == null || !session.isActive()) {
            return Optional.empty();
        }
        session.stop(TraceSessionState.STOPPED, nowMillis);
        clearPending(sessionId);
        notifySessionStopped(sessionId);
        refreshInstrumentationState();
        return Optional.of(sessionId);
    }

    public synchronized List<String> getActiveSessionIdsForEvent(String eventClassName) {
        return sessions.values().stream()
                .filter(session ->
                        session.isActive() && session.getEventClassName().equals(eventClassName))
                .map(TraceSession::getSessionId)
                .toList();
    }

    public synchronized List<String> getActiveEventClassNames() {
        return sessions.values().stream()
                .filter(TraceSession::isActive)
                .map(TraceSession::getEventClassName)
                .distinct()
                .toList();
    }

    public boolean isTracingEnabled() {
        synchronized (this) {
            return sessions.values().stream().anyMatch(TraceSession::isActive);
        }
    }

    public int getActiveSessionCount() {
        synchronized (this) {
            return (int)
                    sessions.values().stream().filter(TraceSession::isActive).count();
        }
    }

    public synchronized Optional<TraceSession> getActiveSession(String sessionId) {
        TraceSession session = sessions.get(sessionId);
        if (session == null || !session.isActive()) {
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public synchronized boolean isThrottledCaptureForEvent(String eventClassName) {
        for (TraceSession session : sessions.values()) {
            if (session.isActive()
                    && session.getEventClassName().equals(eventClassName)
                    && session.isThrottledCapture()) {
                return true;
            }
        }
        return false;
    }

    public synchronized long minSlowThresholdForEvent(String eventClassName) {
        return sessions.values().stream()
                .filter(session ->
                        session.isActive() && session.getEventClassName().equals(eventClassName))
                .mapToLong(session -> session.getConfig().slowThresholdNanos())
                .min()
                .orElse(dev.bellaouzo.eventlens.domain.observability.PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS);
    }

    public void beginEventDispatch(
            String sessionId, long dispatchKey, EventFilterContext context, long nowMillis, long nowNanos) {
        synchronized (this) {
            TraceSession session = sessions.get(sessionId);
            if (session == null) {
                return;
            }
            Optional<TraceSession.PendingDispatch> pending = session.beginDispatch(context, nowMillis, nowNanos);
            pending.ifPresent(value -> TraceSessionDispatchCompleter.pendingMap(pendingDispatches, sessionId)
                    .put(dispatchKey, value));
        }
    }

    public void completeEventDispatch(String sessionId, long dispatchKey, DispatchCompletion completion) {
        TraceSession session;
        Map<Long, TraceSession.PendingDispatch> pendingForSession;
        synchronized (this) {
            session = sessions.get(sessionId);
            if (session == null) {
                return;
            }
            pendingForSession = pendingDispatches.get(sessionId);
            if (pendingForSession == null) {
                return;
            }
        }

        TraceSessionDispatchCompleter.complete(
                session,
                pendingForSession,
                dispatchKey,
                completion,
                dispatchCaptureListener,
                this::refreshInstrumentationState);
    }

    public synchronized void expireSessions(long nowMillis) {
        TraceSessionExpiration.expireActiveSessions(sessions.values(), nowMillis, this::clearPending);
        refreshInstrumentationState();
    }

    public synchronized void closeAll() {
        long nowMillis = System.currentTimeMillis();
        for (TraceSession session : sessions.values()) {
            if (session.isActive()) {
                session.stop(TraceSessionState.STOPPED, nowMillis);
            }
        }
        pendingDispatches.clear();
        sessions.clear();
        if (instrumentationPort != null) {
            instrumentationPort.clearObservationState();
        }
    }

    public InstrumentationPort getInstrumentationPort() {
        return instrumentationPort;
    }

    private void refreshInstrumentationState() {
        TraceSessionInstrumentation.refreshObservationState(instrumentationPort, sessions.values());
    }

    private void notifySessionStopped(String sessionId) {
        if (dispatchCaptureListener instanceof SessionLifecycleListener listener) {
            listener.onSessionStopped(sessionId);
        }
    }

    private void clearPending(String sessionId) {
        pendingDispatches.remove(sessionId);
    }
}
