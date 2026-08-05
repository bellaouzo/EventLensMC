package dev.bellaouzo.eventlens.domain.status;

import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationDiagnostics;
import java.util.Optional;

public record EventLensStatus(
        String version,
        String targetPlatform,
        boolean tracingEnabled,
        int activeSessionCount,
        boolean agentAttached,
        int agentProtocolVersion,
        String timingMode,
        InstrumentationDiagnostics instrumentation,
        Optional<String> agentArgument) {}
