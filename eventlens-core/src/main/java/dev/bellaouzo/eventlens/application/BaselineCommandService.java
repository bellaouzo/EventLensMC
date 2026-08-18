package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import dev.bellaouzo.eventlens.domain.report.TraceRegressionData;
import dev.bellaouzo.eventlens.domain.report.TraceRegressionReport;
import dev.bellaouzo.eventlens.domain.report.TraceReportComparer;
import dev.bellaouzo.eventlens.domain.report.TraceReportDocument;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class BaselineCommandService {

    private final ExportCommandService exportCommandService;
    private final ExportPort exportPort;

    public BaselineCommandService(ExportCommandService exportCommandService, ExportPort exportPort) {
        this.exportCommandService = exportCommandService;
        this.exportPort = exportPort;
    }

    public SaveResult save(String sessionId, String baselineName, ExportRedactionMode redactionMode) {
        Optional<TraceReportDocument> report = exportCommandService.buildReport(sessionId, redactionMode);
        if (report.isEmpty()) {
            return SaveResult.sessionNotFound(sessionId);
        }
        String safeName = sanitizeBaselineName(baselineName);
        if (safeName.isBlank()) {
            return SaveResult.failure("Baseline name must include letters or numbers.");
        }

        TraceRegressionData data = TraceReportComparer.toRegressionData(report.get(), Optional.empty());
        String encoded = BaselineCodec.encode(safeName, data);
        ExportPort.ExportWriteResult write = exportPort.writeBaseline(safeName, encoded);
        if (!write.success()) {
            return SaveResult.failure(write.errorMessage().orElse("Failed to write baseline file."));
        }
        return SaveResult.success(write.path().orElseThrow(), safeName);
    }

    public List<String> list() {
        return exportPort.listBaselines();
    }

    public DeleteResult delete(String baselineName) {
        String safeName = sanitizeBaselineName(baselineName);
        if (safeName.isBlank()) {
            return DeleteResult.failure("Baseline name must include letters or numbers.");
        }
        if (!exportPort.deleteBaseline(safeName)) {
            return DeleteResult.notFound(safeName);
        }
        return DeleteResult.success(safeName);
    }

    public CompareResult compareSession(String sessionId, String baselineName, Optional<String> pluginScope) {
        Optional<TraceReportDocument> session =
                exportCommandService.buildReport(sessionId, ExportRedactionMode.SHARE_SAFE);
        if (session.isEmpty()) {
            return CompareResult.leftNotFound(sessionId);
        }
        Optional<TraceRegressionData> baseline = readBaseline(baselineName, pluginScope);
        if (baseline.isEmpty()) {
            return CompareResult.rightNotFound(baselineName);
        }
        TraceRegressionData left = TraceReportComparer.toRegressionData(session.get(), pluginScope);
        TraceRegressionReport report = TraceReportComparer.compare(left, baseline.get());
        return CompareResult.success(new TraceRegressionReport(
                report.leftSourceId(),
                report.rightSourceId(),
                pluginScope.map(name -> "plugin=" + name).orElse("session vs baseline"),
                report.sameEventClass(),
                report.differences(),
                report.notes()));
    }

    public CompareResult compare(String leftBaseline, String rightBaseline, Optional<String> pluginScope) {
        Optional<TraceRegressionData> left = readBaseline(leftBaseline, pluginScope);
        if (left.isEmpty()) {
            return CompareResult.leftNotFound(leftBaseline);
        }
        Optional<TraceRegressionData> right = readBaseline(rightBaseline, pluginScope);
        if (right.isEmpty()) {
            return CompareResult.rightNotFound(rightBaseline);
        }
        TraceRegressionReport report = TraceReportComparer.compare(left.get(), right.get());
        return CompareResult.success(new TraceRegressionReport(
                report.leftSourceId(),
                report.rightSourceId(),
                pluginScope.map(name -> "plugin=" + name).orElse("all dispatches"),
                report.sameEventClass(),
                report.differences(),
                report.notes()));
    }

    private Optional<TraceRegressionData> readBaseline(String baselineName, Optional<String> pluginScope) {
        String safeName = sanitizeBaselineName(baselineName);
        if (safeName.isBlank()) {
            return Optional.empty();
        }
        return exportPort.readBaseline(safeName).flatMap(content -> BaselineCodec.decode(content, pluginScope));
    }

    private static String sanitizeBaselineName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
    }

    public sealed interface SaveResult {
        record Success(Path path, String baselineName) implements SaveResult {}

        record SessionNotFound(String sessionId) implements SaveResult {}

        record Failure(String message) implements SaveResult {}

        static SaveResult success(Path path, String baselineName) {
            return new Success(path, baselineName);
        }

        static SaveResult sessionNotFound(String sessionId) {
            return new SessionNotFound(sessionId);
        }

        static SaveResult failure(String message) {
            return new Failure(message);
        }
    }

    public sealed interface DeleteResult {
        record Success(String baselineName) implements DeleteResult {}

        record NotFound(String baselineName) implements DeleteResult {}

        record Failure(String message) implements DeleteResult {}

        static DeleteResult success(String baselineName) {
            return new Success(baselineName);
        }

        static DeleteResult notFound(String baselineName) {
            return new NotFound(baselineName);
        }

        static DeleteResult failure(String message) {
            return new Failure(message);
        }
    }

    public sealed interface CompareResult {
        record Success(TraceRegressionReport report) implements CompareResult {}

        record LeftNotFound(String baselineName) implements CompareResult {}

        record RightNotFound(String baselineName) implements CompareResult {}

        static CompareResult success(TraceRegressionReport report) {
            return new Success(report);
        }

        static CompareResult leftNotFound(String baselineName) {
            return new LeftNotFound(baselineName);
        }

        static CompareResult rightNotFound(String baselineName) {
            return new RightNotFound(baselineName);
        }
    }
}
