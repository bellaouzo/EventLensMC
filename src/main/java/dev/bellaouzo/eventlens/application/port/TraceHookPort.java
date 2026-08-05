package dev.bellaouzo.eventlens.application.port;

import dev.bellaouzo.eventlens.trace.TraceSessionManager;

public interface TraceHookPort {

    void registerHooksForEvent(String eventClassName);

    void syncWithActiveSessions(TraceSessionManager traceSessionManager);
}
