package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.application.TraceReportBuilder;
import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionGeneration;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.port.ModEnvironmentPort;
import dev.bellaouzo.eventlens.modcommon.port.ModListenerRegistryPort;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
import java.util.Optional;

public final class ModTraceCoordinator {

    public static final int VIEW_PAGE_SIZE = 5;

    private final TraceSessionManager sessionManager;
    private final TraceReportBuilder reportBuilder;
    private final ExportPort exportPort;
    private final ModListenerRegistryPort listenerRegistryPort;
    private final ModEnvironmentPort environmentPort;
    private String lastSessionId = "";
    private TraceFilter startFilter = TraceFilter.Builder.unrestricted().build();

    public ModTraceCoordinator(
            TraceSessionManager sessionManager,
            TraceReportBuilder reportBuilder,
            ExportPort exportPort,
            ModListenerRegistryPort listenerRegistryPort,
            ModEnvironmentPort environmentPort) {
        this.sessionManager = sessionManager;
        this.reportBuilder = reportBuilder;
        this.exportPort = exportPort;
        this.listenerRegistryPort = listenerRegistryPort;
        this.environmentPort = environmentPort;
    }

    public TraceSessionManager sessionManager() {
        return sessionManager;
    }

    public ModListenerRegistryPort listenerRegistryPort() {
        return listenerRegistryPort;
    }

    public ModEnvironmentPort environmentPort() {
        return environmentPort;
    }

    public void setStartFilter(TraceFilter filter) {
        this.startFilter = filter == null ? TraceFilter.Builder.unrestricted().build() : filter;
    }

    public ModTraceResults.Status status() {
        var instrumentation = sessionManager.getInstrumentationPort();
        boolean agent = instrumentation != null && instrumentation.isAgentPresent();
        return new ModTraceResults.Status(
                environmentPort.eventLensVersion(),
                environmentPort.platformLabel(),
                environmentPort.minecraftVersion(),
                sessionManager.isTracingEnabled(),
                agent,
                agent ? instrumentation.protocolVersion() : 0,
                agent && instrumentation.isProtocolCompatible(),
                agent && instrumentation.listenerSnapshotsEnabled(),
                sessionManager.getActiveSessionCount(),
                sessionManager.listSessions(),
                SupportedModEventTypes.simpleNames());
    }

    public ModTraceResults.StartResult startTrace(
            String eventSimpleName, String ownerName, boolean confirmHot, Optional<Integer> maxEvents) {
        ModTracePresets.ResolvedStart resolved = ModTracePresets.resolveStart(eventSimpleName);
        if (resolved.failed()) {
            return ModTraceResults.StartResult.failure(resolved.error());
        }
        if (resolved.anyHot() && !confirmHot) {
            return ModTraceResults.StartResult.hotConfirmation(resolved.label());
        }
        try {
            int limit = maxEvents.orElse(resolved.anyHot() ? 64 : 256);
            TraceSessionConfig config = new TraceSessionConfig(
                    resolved.classNames(),
                    startFilter,
                    Optional.empty(),
                    Optional.of(limit),
                    dev.bellaouzo.eventlens.domain.observability.PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS,
                    false);
            lastSessionId = sessionManager.startSession(config, ownerName, System.currentTimeMillis());
            return ModTraceResults.StartResult.success(lastSessionId, resolved.label(), resolved.anyHot());
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ModTraceResults.StartResult.failure(ex.getMessage());
        }
    }

    public ModTraceResults.StopResult stopTraces(String ownerName) {
        List<String> stopped = sessionManager.stopSessionsForOwner(ownerName, System.currentTimeMillis());
        if (stopped.isEmpty()) {
            return ModTraceResults.StopResult.failure("No active trace sessions.");
        }
        lastSessionId = stopped.getLast();
        return ModTraceResults.StopResult.success(stopped);
    }

    public ModTraceResults.StopResult stopSession(String sessionId) {
        Optional<String> stopped = sessionManager.stopSession(sessionId, System.currentTimeMillis());
        if (stopped.isEmpty()) {
            return ModTraceResults.StopResult.failure("No active session " + sessionId + ".");
        }
        lastSessionId = stopped.orElseThrow();
        return ModTraceResults.StopResult.success(List.of(lastSessionId));
    }

    public ModTraceResults.PauseResult pauseTraces(String ownerName) {
        List<String> paused = sessionManager.pauseSessionsForOwner(ownerName, System.currentTimeMillis());
        if (paused.isEmpty()) {
            return ModTraceResults.PauseResult.failure("No active session to pause.");
        }
        lastSessionId = paused.getLast();
        return ModTraceResults.PauseResult.paused(paused);
    }

    public ModTraceResults.PauseResult pauseSession(String sessionId) {
        Optional<String> paused = sessionManager.pauseSession(sessionId, System.currentTimeMillis());
        if (paused.isEmpty()) {
            return ModTraceResults.PauseResult.failure("No active session " + sessionId + ".");
        }
        lastSessionId = paused.orElseThrow();
        return ModTraceResults.PauseResult.paused(List.of(lastSessionId));
    }

    public ModTraceResults.PauseResult resumeTraces(String ownerName) {
        List<String> resumed = sessionManager.resumeSessionsForOwner(ownerName, System.currentTimeMillis());
        if (resumed.isEmpty()) {
            return ModTraceResults.PauseResult.failure("No paused session to resume.");
        }
        lastSessionId = resumed.getLast();
        return ModTraceResults.PauseResult.resumed(resumed);
    }

    public ModTraceResults.PauseResult resumeSession(String sessionId) {
        Optional<String> resumed = sessionManager.resumeSession(sessionId, System.currentTimeMillis());
        if (resumed.isEmpty()) {
            return ModTraceResults.PauseResult.failure("No paused session " + sessionId + ".");
        }
        lastSessionId = resumed.orElseThrow();
        return ModTraceResults.PauseResult.resumed(List.of(lastSessionId));
    }

    public ModTraceResults.RestartResult restartSession(String sessionId) {
        return switch (sessionManager.restartSession(sessionId, System.currentTimeMillis())) {
            case TraceRestartResult.Success success -> {
                lastSessionId = success.sessionId();
                yield ModTraceResults.RestartResult.success(
                        success.sessionId(),
                        success.sourceSessionId(),
                        SupportedModEventTypes.displaySimpleName(success.eventClassName()),
                        success.restartCount());
            }
            case TraceRestartResult.NotFound(var missing) ->
                ModTraceResults.RestartResult.failure("No session " + missing + ".");
            case TraceRestartResult.StillOpen(var openId, var state) ->
                ModTraceResults.RestartResult.failure(
                        "Session " + openId + " is still " + state + ". Use resume or stop first.");
            case TraceRestartResult.SessionLimit(var message) -> ModTraceResults.RestartResult.failure(message);
        };
    }

    public List<TraceSessionSummary> listSessions() {
        return sessionManager.listSessions();
    }

    public List<TraceSessionGeneration> listGenerations(String sessionId) {
        return sessionManager.listGenerations(sessionId);
    }

    public ModTraceResults.ViewResult viewSession(String sessionId, int page, Optional<Integer> generation) {
        Optional<TraceSessionDetail> detail = sessionManager.getSessionDetail(sessionId, generation);
        if (detail.isEmpty()) {
            return ModTraceResults.ViewResult.notFound(sessionId);
        }
        List<TraceDispatchRecord> records = detail.orElseThrow().records();
        int totalPages = Math.max(1, (records.size() + VIEW_PAGE_SIZE - 1) / VIEW_PAGE_SIZE);
        if (page < 1 || page > totalPages) {
            return ModTraceResults.ViewResult.invalidPage(page, totalPages);
        }
        int from = (page - 1) * VIEW_PAGE_SIZE;
        int to = Math.min(records.size(), from + VIEW_PAGE_SIZE);
        lastSessionId = sessionId;
        return ModTraceResults.ViewResult.success(
                detail.orElseThrow().summary(), records.subList(from, to), page, totalPages, false);
    }

    public ModTraceResults.ViewResult viewDispatch(String sessionId, int sequence, Optional<Integer> generation) {
        Optional<TraceSessionDetail> detail = sessionManager.getSessionDetail(sessionId, generation);
        if (detail.isEmpty()) {
            return ModTraceResults.ViewResult.notFound(sessionId);
        }
        List<TraceDispatchRecord> match = detail.orElseThrow().records().stream()
                .filter(record -> record.sequence() == sequence)
                .toList();
        if (match.isEmpty()) {
            return ModTraceResults.ViewResult.dispatchNotFound(sessionId, sequence);
        }
        lastSessionId = sessionId;
        return ModTraceResults.ViewResult.success(detail.orElseThrow().summary(), match, 1, 1, true);
    }

    public ModTraceResults.ExportResult exportSession(String sessionId, Optional<Integer> generation) {
        return exportSession(sessionId, generation, ExportFormat.JSON, ExportRedactionMode.SHARE_SAFE);
    }

    public ModTraceResults.ExportResult exportSession(
            String sessionId,
            Optional<Integer> generation,
            ExportFormat format,
            ExportRedactionMode redactionMode) {
        ModTraceResults.ExportResult result = ModSessionExporter.export(
                sessionManager,
                reportBuilder,
                exportPort,
                lastSessionId,
                sessionId,
                generation,
                format,
                redactionMode);
        if (result.success()) {
            lastSessionId = result.sessionId();
        }
        return result;
    }
}
