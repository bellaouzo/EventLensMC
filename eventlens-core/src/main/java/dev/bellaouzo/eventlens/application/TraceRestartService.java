package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;

public final class TraceRestartService {

    private TraceRestartService() {}

    public static TraceRestartResult restart(
            TraceSessionManager sessionManager, TraceHookPort traceHookPort, String sessionId) {
        TraceRestartResult result = sessionManager.restartSession(sessionId, System.currentTimeMillis());
        if (result instanceof TraceRestartResult.Success success) {
            traceHookPort.registerHooksForEvent(success.eventClassName());
            traceHookPort.syncWithActiveSessions(sessionManager);
        }
        return result;
    }
}
