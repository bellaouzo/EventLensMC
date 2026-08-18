package dev.bellaouzo.eventlens.command.trace;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.EventLensReportConfig;
import dev.bellaouzo.eventlens.application.ExportCommandService;
import dev.bellaouzo.eventlens.application.ReportRetentionService;
import dev.bellaouzo.eventlens.application.TraceCorrelateService;
import dev.bellaouzo.eventlens.application.TraceReportBuilder;
import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.command.RecordingCommandSender;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.report.TraceReportEnvironment;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TraceCorrelateCommandHandlerTest {

    private TraceSessionManager sessions;
    private TraceCorrelateService correlateService;

    @BeforeEach
    void setUp() {
        sessions = new TraceSessionManager();
        FakeExportPort exportPort = new FakeExportPort();
        ExportCommandService export = new ExportCommandService(
                sessions,
                new TraceReportBuilder(
                        (plugins, now) -> new TraceReportEnvironment(
                                "test", "25", "Paper 26.2", "1.0.0", "Paper 26.2", Map.of(), now),
                        NO_OP_INSTRUMENTATION,
                        "Paper 26.2"),
                exportPort,
                new ReportRetentionService(exportPort, EventLensReportConfig.defaults()));
        correlateService = new TraceCorrelateService(sessions, export);
    }

    @Test
    void printsUsageWhenSessionsMissing() {
        RecordingCommandSender sender = new RecordingCommandSender(true);
        TraceCorrelateCommandHandler.handle(sender.sender(), new String[] {"trace", "correlate"}, correlateService);
        assertTrue(sender.joined().contains("Usage: /eventlens trace correlate"));
    }

    @Test
    void reportsMissingSession() {
        RecordingCommandSender sender = new RecordingCommandSender(true);
        TraceCorrelateCommandHandler.handle(
                sender.sender(), new String[] {"trace", "correlate", "missing1", "missing2"}, correlateService);
        assertTrue(sender.joined().contains("No trace session"));
    }

    @Test
    void linksZeroPairsForEmptySessions() {
        String left = startSession();
        String right = startSession();
        RecordingCommandSender sender = new RecordingCommandSender(true);
        TraceCorrelateCommandHandler.handle(
                sender.sender(), new String[] {"trace", "correlate", left, right}, correlateService);
        assertTrue(sender.joined().contains("Linked 0 dispatch pair(s)."));
    }

    @Test
    void deniesWithoutPermission() {
        RecordingCommandSender sender = new RecordingCommandSender(false);
        TraceCorrelateCommandHandler.handle(
                sender.sender(), new String[] {"trace", "correlate", "a", "b"}, correlateService);
        assertTrue(sender.joined().contains("You do not have permission."));
    }

    private String startSession() {
        return sessions.startSession(
                new TraceSessionConfig(
                        "org.bukkit.event.player.PlayerInteractEvent",
                        TraceFilter.unrestricted(),
                        Optional.empty(),
                        Optional.empty()),
                "Admin",
                System.currentTimeMillis());
    }

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
            // unused in this handler test
        }

        @Override
        public void clearObservationState() {
            // unused in this handler test
        }
    };

    private static final class FakeExportPort implements ExportPort {
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
            return ExportWriteResult.success(reportsDirectory().resolve(safeBaseName + ".json"));
        }

        @Override
        public ExportWriteResult writeBaseline(String safeBaseName, String content) {
            return ExportWriteResult.failure("unused");
        }

        @Override
        public int deleteReportsOlderThan(long cutoffMillis) {
            return 0;
        }

        @Override
        public Optional<String> readReport(String safeBaseName, ExportFormat format) {
            return Optional.empty();
        }

        @Override
        public Optional<String> readBaseline(String safeBaseName) {
            return Optional.empty();
        }

        @Override
        public List<String> listBaselines() {
            return List.of();
        }

        @Override
        public boolean deleteBaseline(String safeBaseName) {
            return false;
        }
    }
}
