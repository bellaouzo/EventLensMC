package dev.bellaouzo.eventlens.domain.instrumentation;

import java.util.List;

public record InstrumentationDiagnostics(
        InstrumentationMode mode,
        InstrumentationCapabilities capabilities,
        boolean agentPresent,
        int agentProtocolVersion,
        boolean agentProtocolCompatible,
        boolean paperVersionCompatible,
        String paperVersionReported,
        String bukkitVersionReported,
        String expectedPlatform,
        List<InstrumentationDiagnosticLine> lines) {

    public InstrumentationDiagnostics {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
