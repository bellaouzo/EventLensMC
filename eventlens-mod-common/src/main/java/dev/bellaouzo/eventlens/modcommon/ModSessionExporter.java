package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.application.ExportCommandService;
import dev.bellaouzo.eventlens.application.TraceReportBuilder;
import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import dev.bellaouzo.eventlens.domain.report.TraceReportDocument;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionExportBundle;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

final class ModSessionExporter {

    private static final DateTimeFormatter EXPORT_STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    private ModSessionExporter() {}

    static ModTraceResults.ExportResult export(
            TraceSessionManager sessionManager,
            TraceReportBuilder reportBuilder,
            ExportPort exportPort,
            String fallbackSessionId,
            String sessionId,
            Optional<Integer> generation,
            ExportFormat format,
            ExportRedactionMode redactionMode) {
        if (format == ExportFormat.BUNDLE) {
            return ModTraceResults.ExportResult.failure(
                    "Client export does not support --format bundle. Use json, ndjson, text, or html.");
        }
        String targetId = sessionId == null || sessionId.isBlank() ? fallbackSessionId : sessionId;
        if (targetId.isBlank()) {
            return ModTraceResults.ExportResult.failure("No session to export. Start and stop a trace first.");
        }
        Optional<TraceSessionExportBundle> bundle = sessionManager.getExportBundle(targetId, generation);
        if (bundle.isEmpty()) {
            return ModTraceResults.ExportResult.failure("Session not found: " + targetId);
        }
        long nowMillis = System.currentTimeMillis();
        TraceReportDocument document = reportBuilder.build(bundle.orElseThrow(), redactionMode, nowMillis);
        String content = ExportCommandService.serialize(document, format);
        String baseName = "eventlens-" + targetId + "-" + EXPORT_STAMP.format(Instant.ofEpochMilli(nowMillis));
        ExportPort.ExportWriteResult result = exportPort.writeReport(baseName, format, content);
        if (!result.success()) {
            return ModTraceResults.ExportResult.failure(result.errorMessage().orElse("Export failed."));
        }
        return ModTraceResults.ExportResult.success(
                result.path().orElseThrow().toString(), document.dispatches().size(), targetId);
    }
}
