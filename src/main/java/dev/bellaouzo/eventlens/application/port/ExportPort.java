package dev.bellaouzo.eventlens.application.port;

import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ExportPort {

    Path reportsDirectory();

    Path baselinesDirectory();

    ExportWriteResult writeReport(String safeBaseName, ExportFormat format, String content);

    ExportWriteResult writeBaseline(String safeBaseName, String content);

    int deleteReportsOlderThan(long cutoffMillis);

    Optional<String> readReport(String safeBaseName, ExportFormat format);

    Optional<String> readBaseline(String safeBaseName);

    List<String> listBaselines();

    boolean deleteBaseline(String safeBaseName);

    record ExportWriteResult(boolean success, Optional<Path> path, Optional<String> errorMessage) {

        public static ExportWriteResult success(Path path) {
            return new ExportWriteResult(true, Optional.of(path), Optional.empty());
        }

        public static ExportWriteResult failure(String message) {
            return new ExportWriteResult(false, Optional.empty(), Optional.of(message));
        }
    }
}
