package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationCapabilities;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import org.jspecify.annotations.NonNull;

public record TraceReportInstrumentation(
        @NonNull InstrumentationMode mode,
        boolean agentPresent,
        int protocolVersion,
        boolean protocolCompatible,
        boolean paperVersionCompatible,
        @NonNull InstrumentationCapabilities capabilities) {}
