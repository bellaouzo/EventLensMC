package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import dev.bellaouzo.eventlens.domain.report.TraceReportEnvironment;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExportCommandServiceTest {

    private static final InstrumentationPort NO_OP_INSTRUMENTATION = new InstrumentationPort() {
        @Override
        public boolean isAgentPresent() {
            return false;
        }

        @Override
        public int protocolVersion() {
            return 0;
        }

        @Override
        public boolean isProtocolCompatible() {
            return true;
        }

        @Override
        public boolean listenerSnapshotsEnabled() {
            return false;
        }

        @Override
        public void refreshObservationState(boolean tracingEnabled, long slowThresholdNanos, boolean captureStacks) {
            throw new UnsupportedOperationException("Not needed for this test.");
        }

        @Override
        public void clearObservationState() {
            throw new UnsupportedOperationException("Not needed for this test.");
        }
    };

    @Test
    void exportsJsonReportToConfiguredDirectory() {
        TraceSessionManager manager = new TraceSessionManager();
        TraceSessionConfig config = new TraceSessionConfig(
                "org.bukkit.event.player.PlayerInteractEvent",
                TraceFilter.unrestricted(),
                Optional.empty(),
                Optional.empty());
        String sessionId = manager.startSession(config, "Admin", 1_000L);

        FakeExportPort exportPort = new FakeExportPort();
        ExportCommandService service = new ExportCommandService(
                manager,
                new TraceReportBuilder(
                        (plugins, now) -> new TraceReportEnvironment(
                                "test", "25", "Paper 26.2", "1.0.0", "Paper 26.2", Map.of(), now),
                        NO_OP_INSTRUMENTATION,
                        "Paper 26.2"),
                exportPort,
                new ReportRetentionService(exportPort, EventLensReportConfig.defaults()));

        ExportCommandService.ExportResult result =
                service.exportSession(sessionId, ExportFormat.JSON, ExportRedactionMode.SHARE_SAFE);

        assertTrue(result instanceof ExportCommandService.ExportResult.Success);
        assertTrue(exportPort.lastContent.isPresent());
        assertTrue(exportPort.lastContent.get().contains("\"reportVersion\""));
    }

    @Test
    void compareDetectsCapturedCountDifference() {
        TraceSessionManager manager = new TraceSessionManager();
        TraceSessionConfig config = new TraceSessionConfig(
                "org.bukkit.event.player.PlayerInteractEvent",
                TraceFilter.unrestricted(),
                Optional.empty(),
                Optional.empty());
        String left = manager.startSession(config, "Admin", 1_000L);
        String right = manager.startSession(config, "Admin", 1_100L);

        ExportCommandService service = new ExportCommandService(
                manager,
                new TraceReportBuilder(
                        (plugins, now) -> new TraceReportEnvironment(
                                "test", "25", "Paper 26.2", "1.0.0", "Paper 26.2", Map.of(), now),
                        NO_OP_INSTRUMENTATION,
                        "Paper 26.2"),
                new FakeExportPort(),
                new ReportRetentionService(new FakeExportPort(), EventLensReportConfig.defaults()));

        ExportCommandService.CompareResult result =
                service.compareSessions(left, right, ExportRedactionMode.SHARE_SAFE, Optional.empty());
        assertTrue(result instanceof ExportCommandService.CompareResult.Success);
    }

    private static final class FakeExportPort implements ExportPort {
        Optional<String> lastContent = Optional.empty();

        @Override
        public Path reportsDirectory() {
            return Path.of("reports");
        }

        @Override
        public Path baselinesDirectory() {
            return Path.of("baselines");
        }

        @Override
        public ExportWriteResult writeReport(String safeBaseName, ExportFormat format, String content) {
            lastContent = Optional.of(content);
            return ExportWriteResult.success(reportsDirectory().resolve(safeBaseName + "." + format.extension()));
        }

        @Override
        public ExportWriteResult writeBaseline(String safeBaseName, String content) {
            lastContent = Optional.of(content);
            return ExportWriteResult.success(baselinesDirectory().resolve(safeBaseName + ".baseline"));
        }

        @Override
        public int deleteReportsOlderThan(long cutoffMillis) {
            return 0;
        }

        @Override
        public Optional<String> readReport(String safeBaseName, ExportFormat format) {
            return lastContent;
        }

        @Override
        public Optional<String> readBaseline(String safeBaseName) {
            return lastContent;
        }

        @Override
        public java.util.List<String> listBaselines() {
            return java.util.List.of();
        }

        @Override
        public boolean deleteBaseline(String safeBaseName) {
            return false;
        }
    }
}
