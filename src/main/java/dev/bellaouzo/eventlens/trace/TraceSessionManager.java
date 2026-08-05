package dev.bellaouzo.eventlens.trace;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks active trace sessions and whether tracing is enabled.
 * Full listener instrumentation will be added in a later milestone.
 */
public final class TraceSessionManager {

    private final AtomicBoolean tracingEnabled = new AtomicBoolean(false);
    private final AtomicInteger activeSessionCount = new AtomicInteger(0);

    public boolean isTracingEnabled() {
        return tracingEnabled.get();
    }

    public int getActiveSessionCount() {
        return activeSessionCount.get();
    }

    public void closeAll() {
        tracingEnabled.set(false);
        activeSessionCount.set(0);
    }
}
