package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.report.ExportLimits;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import dev.bellaouzo.eventlens.domain.report.TraceRegressionReport;
import dev.bellaouzo.eventlens.domain.report.TraceReportComparer;
import dev.bellaouzo.eventlens.domain.report.TraceReportDocument;
import dev.bellaouzo.eventlens.domain.report.TraceReportHtmlSerializer;
import dev.bellaouzo.eventlens.domain.report.TraceReportJsonSerializer;
import dev.bellaouzo.eventlens.domain.report.TraceReportJsonSupport;
import dev.bellaouzo.eventlens.domain.report.TraceReportNdjsonSerializer;
import dev.bellaouzo.eventlens.domain.report.TraceReportTextSerializer;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class ExportCommandService {

    private final TraceSessionManager traceSessionManager;
    private final TraceReportBuilder traceReportBuilder;
    private final ExportPort exportPort;
    private final ReportRetentionService reportRetentionService;
    private final AtomicInteger pendingExports = new AtomicInteger();

    public ExportCommandService(
            TraceSessionManager traceSessionManager,
            TraceReportBuilder traceReportBuilder,
            ExportPort exportPort,
            ReportRetentionService reportRetentionService) {
        this.traceSessionManager = traceSessionManager;
        this.traceReportBuilder = traceReportBuilder;
        this.exportPort = exportPort;
        this.reportRetentionService = reportRetentionService;
    }

    public Optional<TraceReportDocument> buildReport(String sessionId, ExportRedactionMode redactionMode) {
        traceSessionManager.expireSessions(System.currentTimeMillis());
        return traceSessionManager
                .getExportBundle(sessionId, Optional.empty())
                .map(bundle -> traceReportBuilder.build(bundle, redactionMode, System.currentTimeMillis()));
    }

    public ExportResult exportSession(String sessionId, ExportFormat format, ExportRedactionMode redactionMode) {
        if (pendingExports.get() >= ExportLimits.MAX_PENDING_EXPORTS) {
            return ExportResult.failure(ExportResult.Reason.PENDING_LIMIT, "Too many exports in progress.");
        }

        Optional<TraceReportDocument> report = buildReport(sessionId, redactionMode);
        if (report.isEmpty()) {
            return ExportResult.failure(
                    ExportResult.Reason.SESSION_NOT_FOUND, "No trace session \"" + sessionId + "\".");
        }

        String content = serialize(report.get(), format);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > ExportLimits.MAX_EXPORT_FILE_BYTES) {
            return ExportResult.failure(
                    ExportResult.Reason.FILE_TOO_LARGE,
                    "Export exceeds " + (ExportLimits.MAX_EXPORT_FILE_BYTES / (1024 * 1024)) + " MiB limit.");
        }

        pendingExports.incrementAndGet();
        try {
            String baseName = safeBaseName(sessionId);
            ExportPort.ExportWriteResult writeResult = exportPort.writeReport(baseName, format, content);
            if (!writeResult.success()) {
                return ExportResult.failure(
                        ExportResult.Reason.WRITE_FAILED,
                        writeResult.errorMessage().orElse("Failed to write export file."));
            }
            reportRetentionService.cleanupIfEnabled();
            Path path = writeResult.path().orElseThrow();
            return ExportResult.success(path, path.getFileName().toString(), format, redactionMode);
        } finally {
            pendingExports.decrementAndGet();
        }
    }

    public Optional<String> compactReport(String sessionId, ExportRedactionMode redactionMode) {
        return buildReport(sessionId, redactionMode).map(TraceReportTextSerializer::compact);
    }

    public CopyDispatchResult compactDispatchReport(
            String sessionId, long dispatchSequence, ExportRedactionMode redactionMode) {
        Optional<TraceReportDocument> report = buildReport(sessionId, redactionMode);
        if (report.isEmpty()) {
            return CopyDispatchResult.sessionNotFound(sessionId);
        }

        Optional<TraceDispatchRecord> dispatchRecord = report.get().dispatches().stream()
                .filter(dispatch -> dispatch.sequence() == dispatchSequence)
                .findFirst();
        if (dispatchRecord.isEmpty()) {
            return CopyDispatchResult.dispatchNotFound(dispatchSequence);
        }

        return CopyDispatchResult.success(
                TraceReportTextSerializer.compactDispatch(report.get(), dispatchRecord.get()));
    }

    public CompareResult compareSessions(
            String leftSessionId,
            String rightSessionId,
            ExportRedactionMode redactionMode,
            Optional<String> pluginScope) {
        Optional<TraceReportDocument> left = buildReport(leftSessionId, redactionMode);
        if (left.isEmpty()) {
            return CompareResult.leftNotFound(leftSessionId);
        }
        Optional<TraceReportDocument> right = buildReport(rightSessionId, redactionMode);
        if (right.isEmpty()) {
            return CompareResult.rightNotFound(rightSessionId);
        }
        Optional<String> normalizedPluginScope = pluginScope.map(String::trim).filter(value -> !value.isEmpty());
        TraceRegressionReport comparison = TraceReportComparer.compare(left.get(), right.get(), normalizedPluginScope);
        if (normalizedPluginScope.isPresent()) {
            String plugin = normalizedPluginScope.get();
            boolean anyLeft = left.get().dispatches().stream().anyMatch(dispatch -> dispatch.listenerChain().stream()
                    .anyMatch(listener -> listener.pluginName().equalsIgnoreCase(plugin)));
            boolean anyRight = right.get().dispatches().stream().anyMatch(dispatch -> dispatch.listenerChain().stream()
                    .anyMatch(listener -> listener.pluginName().equalsIgnoreCase(plugin)));
            if (!anyLeft && !anyRight) {
                return CompareResult.pluginNotFound(plugin);
            }
        }
        return CompareResult.success(comparison);
    }

    public static String serialize(TraceReportDocument document, ExportFormat format) {
        return switch (format) {
            case JSON -> TraceReportJsonSerializer.serialize(document);
            case NDJSON -> TraceReportNdjsonSerializer.serialize(document);
            case TEXT -> TraceReportTextSerializer.serialize(document);
            case HTML -> TraceReportHtmlSerializer.serialize(document);
            case BUNDLE -> TraceReportJsonSupport.minifyJson(TraceReportJsonSerializer.serialize(document));
        };
    }

    private static String safeBaseName(String sessionId) {
        String sanitized = sessionId.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "");
        if (sanitized.isBlank()) {
            sanitized = "trace";
        }
        return "eventlens-trace-" + sanitized + "-" + System.currentTimeMillis();
    }

    public sealed interface ExportResult {
        record Success(Path path, String fileName, ExportFormat format, ExportRedactionMode redactionMode)
                implements ExportResult {}

        record Failure(Reason reason, String message) implements ExportResult {}

        enum Reason {
            SESSION_NOT_FOUND,
            FILE_TOO_LARGE,
            PENDING_LIMIT,
            WRITE_FAILED
        }

        static ExportResult success(
                Path path, String fileName, ExportFormat format, ExportRedactionMode redactionMode) {
            return new ExportResult.Success(path, fileName, format, redactionMode);
        }

        static ExportResult failure(Reason reason, String message) {
            return new Failure(reason, message);
        }
    }

    public sealed interface CompareResult {
        record Success(TraceRegressionReport comparison) implements CompareResult {}

        record LeftNotFound(String sessionId) implements CompareResult {}

        record RightNotFound(String sessionId) implements CompareResult {}

        record PluginNotFound(String pluginName) implements CompareResult {}

        static CompareResult success(TraceRegressionReport comparison) {
            return new CompareResult.Success(comparison);
        }

        static CompareResult leftNotFound(String sessionId) {
            return new LeftNotFound(sessionId);
        }

        static CompareResult rightNotFound(String sessionId) {
            return new RightNotFound(sessionId);
        }

        static CompareResult pluginNotFound(String pluginName) {
            return new PluginNotFound(pluginName);
        }
    }

    public sealed interface CopyDispatchResult {
        record Success(String report) implements CopyDispatchResult {}

        record SessionNotFound(String sessionId) implements CopyDispatchResult {}

        record DispatchNotFound(long sequence) implements CopyDispatchResult {}

        static CopyDispatchResult success(String report) {
            return new Success(report);
        }

        static CopyDispatchResult sessionNotFound(String sessionId) {
            return new SessionNotFound(sessionId);
        }

        static CopyDispatchResult dispatchNotFound(long sequence) {
            return new DispatchNotFound(sequence);
        }
    }
}
