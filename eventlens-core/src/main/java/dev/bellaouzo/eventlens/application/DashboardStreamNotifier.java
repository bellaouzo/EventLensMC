package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.report.DashboardLiveUpdateSerializer;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.trace.DispatchCaptureListener;
import dev.bellaouzo.eventlens.trace.SessionLifecycleListener;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;

public final class DashboardStreamNotifier implements DispatchCaptureListener, SessionLifecycleListener {

    private final DashboardStreamHub streamHub;
    private final TraceSessionManager traceSessionManager;

    public DashboardStreamNotifier(DashboardStreamHub streamHub, TraceSessionManager traceSessionManager) {
        this.streamHub = streamHub;
        this.traceSessionManager = traceSessionManager;
    }

    @Override
    public void onDispatchCaptured(String sessionId, TraceDispatchRecord dispatchRecord) {
        traceSessionManager
                .getSessionDetail(sessionId)
                .ifPresent(detail -> streamHub.publish(
                        "dispatch",
                        DashboardLiveUpdateSerializer.serializeDispatchUpdate(detail.summary(), dispatchRecord)));
    }

    @Override
    public void onSessionStarted(String sessionId) {
        traceSessionManager
                .getSessionDetail(sessionId)
                .ifPresent(detail -> streamHub.publish(
                        "session-started", DashboardLiveUpdateSerializer.serializeSessionStarted(detail.summary())));
    }

    @Override
    public void onSessionStopped(String sessionId) {
        traceSessionManager
                .getSessionDetail(sessionId)
                .ifPresent(detail -> streamHub.publish(
                        "session-stopped", DashboardLiveUpdateSerializer.serializeSessionStarted(detail.summary())));
    }
}
