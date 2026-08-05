package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.observability.PerformanceBudget;
import java.util.Collection;

final class TraceSessionInstrumentation {

    private TraceSessionInstrumentation() {}

    static boolean isAgentPresent(InstrumentationPort instrumentationPort) {
        return instrumentationPort != null && instrumentationPort.isAgentPresent();
    }

    static void refreshObservationState(InstrumentationPort instrumentationPort, Collection<TraceSession> sessions) {
        if (instrumentationPort == null) {
            return;
        }
        boolean tracingEnabled = sessions.stream().anyMatch(TraceSession::isActive);
        long slowThreshold = sessions.stream()
                .filter(TraceSession::isActive)
                .mapToLong(session -> session.getConfig().slowThresholdNanos())
                .min()
                .orElse(PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS);
        boolean captureStacks = sessions.stream().filter(TraceSession::isActive).anyMatch(session -> session.getConfig()
                .captureStacks());
        instrumentationPort.refreshObservationState(tracingEnabled, slowThreshold, captureStacks);
    }
}
