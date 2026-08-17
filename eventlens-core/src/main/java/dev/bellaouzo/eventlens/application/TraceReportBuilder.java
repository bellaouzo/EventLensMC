package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.EnvironmentPort;
import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationCapabilities;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import dev.bellaouzo.eventlens.domain.report.ExportLimits;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import dev.bellaouzo.eventlens.domain.report.TraceReportDocument;
import dev.bellaouzo.eventlens.domain.report.TraceReportEnvironment;
import dev.bellaouzo.eventlens.domain.report.TraceReportInstrumentation;
import dev.bellaouzo.eventlens.domain.report.TraceReportRedactor;
import dev.bellaouzo.eventlens.domain.report.TraceReportWarnings;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionExportBundle;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class TraceReportBuilder {

    private final EnvironmentPort environmentPort;
    private final InstrumentationPort instrumentationPort;
    private final String expectedPlatform;

    public TraceReportBuilder(
            EnvironmentPort environmentPort, InstrumentationPort instrumentationPort, String expectedPlatform) {
        this.environmentPort = environmentPort;
        this.instrumentationPort = instrumentationPort;
        this.expectedPlatform = expectedPlatform;
    }

    public TraceReportDocument build(
            TraceSessionExportBundle bundle, ExportRedactionMode redactionMode, long nowMillis) {
        Set<String> pluginNames = collectPluginNames(bundle);
        TraceReportEnvironment environment = environmentPort.capture(pluginNames, nowMillis);
        TraceReportInstrumentation instrumentation = captureInstrumentation(environment);
        TraceReportDocument document = new TraceReportDocument(
                ExportLimits.REPORT_VERSION,
                redactionMode,
                environment,
                instrumentation,
                bundle.summary(),
                Optional.ofNullable(bundle.summary().timingSummary()),
                bundle.config().filter(),
                List.of(),
                bundle.records());
        List<String> warnings = TraceReportWarnings.collect(document);
        document = new TraceReportDocument(
                document.reportVersion(),
                document.redactionMode(),
                document.environment(),
                document.instrumentation(),
                document.summary(),
                document.sessionTimingSummary(),
                document.filter(),
                warnings,
                document.dispatches());
        return TraceReportRedactor.apply(document, redactionMode);
    }

    private TraceReportInstrumentation captureInstrumentation(TraceReportEnvironment environment) {
        boolean agentPresent = instrumentationPort.isAgentPresent();
        int protocolVersion = agentPresent ? instrumentationPort.protocolVersion() : 0;
        boolean protocolCompatible = instrumentationPort.isProtocolCompatible();
        boolean snapshotsEnabled = instrumentationPort.listenerSnapshotsEnabled();
        InstrumentationMode mode = resolveMode(agentPresent, protocolCompatible, snapshotsEnabled);
        InstrumentationCapabilities capabilities = resolveCapabilities(mode, snapshotsEnabled);
        boolean paperVersionCompatible = PaperVersionSupport.isCompatible(
                expectedPlatform, environment.paperVersion(), environment.serverVersion());
        return new TraceReportInstrumentation(
                mode, agentPresent, protocolVersion, protocolCompatible, paperVersionCompatible, capabilities);
    }

    private static InstrumentationMode resolveMode(
            boolean agentPresent, boolean protocolCompatible, boolean snapshotsEnabled) {
        if (!agentPresent) {
            return InstrumentationMode.DISPATCH_ONLY;
        }
        if (!protocolCompatible || !snapshotsEnabled) {
            return InstrumentationMode.DEGRADED;
        }
        return InstrumentationMode.PRECISE;
    }

    private static InstrumentationCapabilities resolveCapabilities(InstrumentationMode mode, boolean snapshotsEnabled) {
        return switch (mode) {
            case PRECISE -> InstrumentationCapabilities.precise();
            case DISPATCH_ONLY -> InstrumentationCapabilities.dispatchOnly();
            case DEGRADED -> InstrumentationCapabilities.degraded(snapshotsEnabled);
        };
    }

    private static Set<String> collectPluginNames(TraceSessionExportBundle bundle) {
        Set<String> pluginNames = new LinkedHashSet<>();
        bundle.config().filter().pluginName().ifPresent(pluginNames::add);
        for (TraceDispatchRecord dispatch : bundle.records()) {
            for (TraceListenerSnapshot listener : dispatch.listenerChain()) {
                pluginNames.add(listener.pluginName());
            }
            for (var timing : dispatch.listenerTimings()) {
                pluginNames.add(timing.pluginName());
            }
        }
        bundle.summary()
                .conflictSummary()
                .investigationTargets()
                .forEach(target -> pluginNames.add(target.pluginName()));
        return pluginNames;
    }
}
