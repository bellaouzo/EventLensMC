package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.DashboardServerContextPort;
import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraph;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardReportEntry;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardSessionEntry;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardStatusPayload;
import dev.bellaouzo.eventlens.domain.report.DashboardJsonSerializer;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import dev.bellaouzo.eventlens.domain.report.TraceReportDocument;
import dev.bellaouzo.eventlens.domain.report.TraceReportJsonSerializer;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public final class DashboardQueryService {

    private final TraceSessionManager traceSessionManager;
    private final ExportCommandService exportCommandService;
    private final ExportPort exportPort;
    private final ListenerRegistryPort listenerRegistryPort;
    private final InstrumentationPort instrumentationPort;
    private final DashboardServerContextPort serverContextPort;
    private final EventLensDashboardConfig dashboardConfig;

    public DashboardQueryService(
            TraceSessionManager traceSessionManager,
            ExportCommandService exportCommandService,
            ExportPort exportPort,
            ListenerRegistryPort listenerRegistryPort,
            InstrumentationPort instrumentationPort,
            DashboardServerContextPort serverContextPort,
            EventLensDashboardConfig dashboardConfig) {
        this.traceSessionManager = traceSessionManager;
        this.exportCommandService = exportCommandService;
        this.exportPort = exportPort;
        this.listenerRegistryPort = listenerRegistryPort;
        this.instrumentationPort = instrumentationPort;
        this.serverContextPort = serverContextPort;
        this.dashboardConfig = dashboardConfig;
    }

    public String statusJson() {
        var server = serverContextPort.capture();
        var activeSession = newestActiveSession();
        return DashboardJsonSerializer.serializeStatus(new DashboardStatusPayload(
                dashboardConfig.enabled(),
                dashboardConfig.port(),
                dashboardConfig.bindAddress(),
                instrumentationPort.isAgentPresent(),
                instrumentationPort.isAgentPresent() ? instrumentationPort.protocolVersion() : 0,
                server,
                activeSession.map(TraceSessionSummary::sessionId).orElse(""),
                activeSession.map(TraceSessionSummary::startedAtMillis).orElse(0L),
                activeSession.map(TraceSessionSummary::capturedEvents).orElse(0),
                activeSession.map(TraceSessionSummary::eventClassName).orElse("")));
    }

    private Optional<TraceSessionSummary> newestActiveSession() {
        return traceSessionManager.listSessions().stream()
                .filter(summary -> summary.state() == TraceSessionState.ACTIVE)
                .max(Comparator.comparingLong(TraceSessionSummary::startedAtMillis));
    }

    public String sessionsJson() {
        List<DashboardSessionEntry> sessions = traceSessionManager.listSessions().stream()
                .sorted(Comparator.comparingLong(TraceSessionSummary::startedAtMillis)
                        .reversed())
                .map(summary -> new DashboardSessionEntry(
                        summary.sessionId(),
                        summary.eventClassName(),
                        summary.state().name(),
                        summary.capturedEvents(),
                        summary.startedAtMillis()))
                .toList();
        return DashboardJsonSerializer.serializeSessions(sessions);
    }

    public Optional<String> sessionReportJson(String sessionId) {
        Optional<TraceReportDocument> report = exportCommandService.buildReport(sessionId, ExportRedactionMode.FULL);
        return report.map(TraceReportJsonSerializer::serialize);
    }

    public String reportsJson() {
        return DashboardJsonSerializer.serializeReports(listReports());
    }

    public Optional<String> readReportFile(String fileName) {
        if (!isSafeReportFileName(fileName)) {
            return Optional.empty();
        }
        Path reportsDirectory = exportPort.reportsDirectory();
        Path target = reportsDirectory.resolve(fileName).normalize();
        if (!target.startsWith(reportsDirectory) || !Files.isRegularFile(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(target, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public DashboardGraph eventRelationshipGraph() {
        return DashboardGraphBuilder.buildEventRelationshipGraph(listenerRegistryPort);
    }

    public DashboardGraph pluginInteractionGraph(Optional<String> sessionId) {
        return DashboardGraphBuilder.buildPluginInteractionGraph(listenerRegistryPort, traceSessionManager, sessionId);
    }

    public String eventRelationshipGraphJson() {
        return DashboardJsonSerializer.serializeGraph(eventRelationshipGraph());
    }

    public String pluginInteractionGraphJson(Optional<String> sessionId) {
        return DashboardJsonSerializer.serializeGraph(pluginInteractionGraph(sessionId));
    }

    private List<DashboardReportEntry> listReports() {
        Path reportsDirectory = exportPort.reportsDirectory();
        if (!Files.isDirectory(reportsDirectory)) {
            return List.of();
        }
        List<DashboardReportEntry> reports = new ArrayList<>();
        try (Stream<Path> paths = Files.list(reportsDirectory)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                toReportEntry(path).ifPresent(reports::add);
            }
        } catch (IOException ignored) {
            return List.of();
        }
        reports.sort(
                Comparator.comparing(DashboardReportEntry::lastModifiedMillis).reversed());
        return List.copyOf(reports);
    }

    static boolean isSafeReportFileName(String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }
        return !fileName.contains("..");
    }

    private Optional<DashboardReportEntry> toReportEntry(Path path) throws IOException {
        String fileName = path.getFileName().toString();
        if (!isSafeReportFileName(fileName)) {
            return Optional.empty();
        }
        String extension = extension(fileName);
        if (!extension.equals("json") && !extension.equals("ndjson") && !extension.equals("html")) {
            return Optional.empty();
        }
        return Optional.of(new DashboardReportEntry(
                fileName, Files.getLastModifiedTime(path).toMillis(), Files.size(path), extension));
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
