package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.application.port.InstrumentationTestPort;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationDiagnostics;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import dev.bellaouzo.eventlens.domain.status.EventLensStatus;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;

public final class StatusQueryService {

    private final TraceSessionManager traceSessionManager;
    private final InstrumentationQueryService instrumentationQueryService;
    private final InstrumentationTestPort instrumentationTestPort;
    private final String version;
    private final String targetPlatform;

    public StatusQueryService(
            TraceSessionManager traceSessionManager,
            InstrumentationPort instrumentationPort,
            InstrumentationTestPort instrumentationTestPort,
            String version,
            String targetPlatform) {
        this.traceSessionManager = traceSessionManager;
        this.instrumentationQueryService = new InstrumentationQueryService(instrumentationPort, targetPlatform);
        this.instrumentationTestPort = instrumentationTestPort;
        this.version = version;
        this.targetPlatform = targetPlatform;
    }

    public EventLensStatus queryStatus(String paperVersionReported, String bukkitVersionReported) {
        InstrumentationDiagnostics instrumentation =
                instrumentationQueryService.query(paperVersionReported, bukkitVersionReported);
        return new EventLensStatus(
                version,
                targetPlatform,
                traceSessionManager.isTracingEnabled(),
                traceSessionManager.getActiveSessionCount(),
                instrumentation.agentPresent(),
                instrumentation.agentProtocolVersion(),
                timingMode(instrumentation.mode()),
                instrumentation,
                instrumentationTestPort.resolveAgentArgument());
    }

    private static String timingMode(InstrumentationMode mode) {
        return switch (mode) {
            case PRECISE -> "PRECISE";
            case DEGRADED -> "DEGRADED";
            case DISPATCH_ONLY -> "DISPATCH_ONLY";
        };
    }
}
