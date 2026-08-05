package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationCapabilities;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public record TraceReportDocument(
        @NonNull String reportVersion,
        @NonNull ExportRedactionMode redactionMode,
        @NonNull TraceReportEnvironment environment,
        @NonNull TraceReportInstrumentation instrumentation,
        @NonNull TraceSessionSummary summary,
        @NonNull Optional<SessionTimingSummary> sessionTimingSummary,
        @NonNull TraceFilter filter,
        @NonNull List<String> warnings,
        @NonNull List<TraceDispatchRecord> dispatches) {

    public TraceReportDocument {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        dispatches = dispatches == null ? List.of() : List.copyOf(dispatches);
    }

    public TraceReportDocument(
            String reportVersion,
            ExportRedactionMode redactionMode,
            TraceReportEnvironment environment,
            TraceSessionSummary summary,
            TraceFilter filter,
            List<String> warnings,
            List<TraceDispatchRecord> dispatches) {
        this(
                reportVersion,
                redactionMode,
                environment,
                new TraceReportInstrumentation(
                        InstrumentationMode.DISPATCH_ONLY,
                        false,
                        0,
                        true,
                        true,
                        InstrumentationCapabilities.dispatchOnly()),
                summary,
                Optional.ofNullable(summary.timingSummary()),
                filter,
                warnings,
                dispatches);
    }

    public TraceReportDocument(
            String reportVersion,
            ExportRedactionMode redactionMode,
            TraceReportEnvironment environment,
            TraceReportInstrumentation instrumentation,
            TraceSessionSummary summary,
            TraceFilter filter,
            List<String> warnings,
            List<TraceDispatchRecord> dispatches) {
        this(
                reportVersion,
                redactionMode,
                environment,
                instrumentation,
                summary,
                Optional.ofNullable(summary.timingSummary()),
                filter,
                warnings,
                dispatches);
    }
}
