package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.List;

public final class CompositeDispatchCaptureListener implements DispatchCaptureListener, SessionLifecycleListener {

    private final List<DispatchCaptureListener> captureListeners;
    private final List<SessionLifecycleListener> lifecycleListeners;

    public CompositeDispatchCaptureListener(
            List<DispatchCaptureListener> captureListeners, List<SessionLifecycleListener> lifecycleListeners) {
        this.captureListeners = List.copyOf(captureListeners);
        this.lifecycleListeners = List.copyOf(lifecycleListeners);
    }

    @Override
    public void onDispatchCaptured(String sessionId, TraceDispatchRecord dispatchRecord) {
        for (DispatchCaptureListener listener : captureListeners) {
            listener.onDispatchCaptured(sessionId, dispatchRecord);
        }
    }

    @Override
    public void onSessionStarted(String sessionId) {
        for (SessionLifecycleListener listener : lifecycleListeners) {
            listener.onSessionStarted(sessionId);
        }
    }

    @Override
    public void onSessionStopped(String sessionId) {
        for (SessionLifecycleListener listener : lifecycleListeners) {
            listener.onSessionStopped(sessionId);
        }
    }
}
