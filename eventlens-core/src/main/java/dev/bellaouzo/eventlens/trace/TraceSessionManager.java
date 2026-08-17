package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.trace.EventFilterContext;
import dev.bellaouzo.eventlens.domain.trace.TraceLimits;
import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionExportBundle;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionGeneration;
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
    private final TraceSessionArchives archives = new TraceSessionArchives();
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
        if (sessions.values().stream().filter(TraceSession::isOpen).count() >= TraceLimits.MAX_CONCURRENT_SESSIONS) {
            throw new IllegalStateException("Concurrent session limit reached.");
        }
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        return TraceSessionSlots.insert(this, sessionId, config, ownerName, nowMillis, 0);
    }

    public synchronized List<TraceSessionSummary> listSessions() {
        return sessions.values().stream()
                .map(session -> session.toSummary(agentPresent()))
                .toList();
    }

    public synchronized Optional<TraceSessionExportBundle> getExportBundle(
            String sessionId, Optional<Integer> generation) {
        return archives.exportBundle(sessions, sessionId, generation, agentPresent());
    }

    public synchronized Optional<TraceSessionConfig> getSessionConfig(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId)).map(TraceSession::getConfig);
    }

    public synchronized Optional<TraceSessionDetail> getSessionDetail(String sessionId) {
        return getSessionDetail(sessionId, Optional.empty());
    }

    public synchronized Optional<TraceSessionDetail> getSessionDetail(String sessionId, Optional<Integer> generation) {
        return archives.detail(sessions, sessionId, generation, agentPresent());
    }

    public synchronized List<TraceSessionGeneration> listGenerations(String sessionId) {
        return archives.listWithCurrent(sessions, sessionId, agentPresent());
    }

    public synchronized List<String> stopSessionsForOwner(String ownerName, long nowMillis) {
        List<String> stopped = new ArrayList<>();
        for (TraceSession session : sessions.values()) {
            if (session.isOpen() && session.getOwnerName().equalsIgnoreCase(ownerName)) {
                session.stop(TraceSessionState.STOPPED, nowMillis);
                stopped.add(session.getSessionId());
                clearPending(session.getSessionId());
                notifyLifecycle(session.getSessionId(), false);
            }
        }
        refreshAfterMutation();
        return stopped;
    }

    public synchronized Optional<String> stopSession(String sessionId, long nowMillis) {
        TraceSession session = sessions.get(sessionId);
        if (session == null || !session.isOpen()) {
            return Optional.empty();
        }
        session.stop(TraceSessionState.STOPPED, nowMillis);
        clearPending(sessionId);
        notifyLifecycle(sessionId, false);
        refreshAfterMutation();
        return Optional.of(sessionId);
    }

    public synchronized List<String> pauseSessionsForOwner(String ownerName, long nowMillis) {
        List<String> paused = TraceSessionControl.changeOwner(
                sessions.values(), ownerName, nowMillis, TraceSession::pause, this::clearPending);
        refreshAfterMutation();
        return paused;
    }

    public synchronized Optional<String> pauseSession(String sessionId, long nowMillis) {
        Optional<String> paused =
                TraceSessionControl.changeOne(sessions.get(sessionId), nowMillis, TraceSession::pause);
        paused.ifPresent(this::clearPending);
        refreshAfterMutation();
        return paused;
    }

    public synchronized List<String> resumeSessionsForOwner(String ownerName, long nowMillis) {
        List<String> resumed = TraceSessionControl.changeOwner(
                sessions.values(), ownerName, nowMillis, TraceSession::resume, ignored -> {});
        refreshAfterMutation();
        return resumed;
    }

    public synchronized Optional<String> resumeSession(String sessionId, long nowMillis) {
        Optional<String> resumed =
                TraceSessionControl.changeOne(sessions.get(sessionId), nowMillis, TraceSession::resume);
        refreshAfterMutation();
        return resumed;
    }

    public synchronized TraceRestartResult restartSession(String sessionId, long nowMillis) {
        return TraceSessionRestart.restart(this, sessions, sessionId, nowMillis);
    }

    public synchronized List<String> getActiveSessionIdsForEvent(String eventClassName) {
        return TraceSessionQueries.activeSessionIdsForEvent(sessions.values(), eventClassName);
    }

    public synchronized List<String> getActiveEventClassNames() {
        return TraceSessionQueries.activeEventClassNames(sessions.values());
    }

    public synchronized boolean isTracingEnabled() {
        return sessions.values().stream().anyMatch(TraceSession::isActive);
    }

    public synchronized int getActiveSessionCount() {
        return (int) sessions.values().stream().filter(TraceSession::isActive).count();
    }

    public synchronized Optional<TraceSession> getActiveSession(String sessionId) {
        TraceSession session = sessions.get(sessionId);
        return session != null && session.isActive() ? Optional.of(session) : Optional.empty();
    }

    public synchronized boolean isThrottledCaptureForEvent(String eventClassName) {
        return TraceSessionQueries.throttledCaptureForEvent(sessions.values(), eventClassName);
    }

    public synchronized long minSlowThresholdForEvent(String eventClassName) {
        return TraceSessionQueries.minSlowThresholdForEvent(sessions.values(), eventClassName);
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
                this::refreshAfterMutation);
    }

    public synchronized void expireSessions(long nowMillis) {
        TraceSessionExpiration.expireActiveSessions(sessions.values(), nowMillis, this::clearPending);
        refreshAfterMutation();
    }

    public synchronized void closeAll() {
        long nowMillis = System.currentTimeMillis();
        for (TraceSession session : sessions.values()) {
            if (session.isOpen()) {
                session.stop(TraceSessionState.STOPPED, nowMillis);
            }
        }
        pendingDispatches.clear();
        sessions.clear();
        archives.clear();
        if (instrumentationPort != null) {
            instrumentationPort.clearObservationState();
        }
    }

    public InstrumentationPort getInstrumentationPort() {
        return instrumentationPort;
    }

    void archiveCurrent(String sessionId) {
        Optional.ofNullable(sessions.get(sessionId)).ifPresent(session -> archives.archive(session, agentPresent()));
    }

    void refreshAfterMutation() {
        TraceSessionInstrumentation.refreshObservationState(instrumentationPort, sessions.values());
    }

    private boolean agentPresent() {
        return TraceSessionInstrumentation.isAgentPresent(instrumentationPort);
    }

    Map<String, TraceSession> sessions() {
        return sessions;
    }

    Map<String, Map<Long, TraceSession.PendingDispatch>> pendingDispatches() {
        return pendingDispatches;
    }

    void notifyLifecycle(String sessionId, boolean started) {
        if (dispatchCaptureListener instanceof SessionLifecycleListener listener) {
            if (started) listener.onSessionStarted(sessionId);
            else listener.onSessionStopped(sessionId);
        }
    }

    void clearPending(String sessionId) {
        pendingDispatches.remove(sessionId);
    }
}
