package dev.bellaouzo.eventlens.domain.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationCapabilities;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraceReportWarningsTest {

    @Test
    void avoidsRepeatingSessionPartialReasonOnEveryDispatch() {
        TraceReportDocument document = documentWithAgentAbsentTiming();
        List<String> warnings = TraceReportWarnings.collect(document);
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("Java agent absent"));
    }

    @Test
    void formatsPartialReasonsForHumanReaders() {
        String formatted = ReportFormatting.formatPartialReasons(EnumSet.of(TracePartialReason.AGENT_ABSENT));
        assertTrue(formatted.contains("Java agent absent"));
        assertFalse(formatted.contains("AGENT_ABSENT"));
    }

    @Test
    void labelsEventLensCheckpointListeners() {
        String display = ListenerDisplayFormatter.format(
                "EventLens",
                "dev.bellaouzo.eventlens.paper.PaperTraceHookManager$1",
                "dev.bellaouzo.eventlens.paper.PaperTraceHookManager$$Lambda/0x1@abc");
        assertEquals("EventLens trace checkpoint", display);
    }

    @Test
    void suppressesStaleAgentAbsentWarningsWhenAgentPresent() {
        TraceReportDocument document = documentWithAgentAbsentTiming();
        TraceReportDocument withAgent = new TraceReportDocument(
                document.reportVersion(),
                document.redactionMode(),
                document.environment(),
                new TraceReportInstrumentation(
                        InstrumentationMode.PRECISE, true, 2, true, true, InstrumentationCapabilities.precise()),
                document.summary(),
                document.sessionTimingSummary(),
                document.filter(),
                document.warnings(),
                document.dispatches());

        List<String> warnings = TraceReportWarnings.collect(withAgent);

        assertTrue(warnings.stream().noneMatch(warning -> warning.contains("Java agent absent")));
    }

    private static TraceReportDocument documentWithAgentAbsentTiming() {
        TraceDispatchRecord dispatch = new TraceDispatchRecord(
                1L,
                1L,
                1L,
                1L,
                0L,
                "org.bukkit.event.player.PlayerInteractEvent",
                true,
                false,
                false,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                EnumSet.of(TracePartialReason.AGENT_ABSENT));
        SessionTimingSummary timing = new SessionTimingSummary(
                DurationStats.empty(),
                DurationStats.empty(),
                List.of(),
                List.of(),
                List.of(),
                0,
                EnumSet.of(TracePartialReason.AGENT_ABSENT),
                1_000_000L,
                false);
        TraceSessionSummary summary = new TraceSessionSummary(
                "abc12345",
                "org.bukkit.event.player.PlayerInteractEvent",
                TraceSessionState.STOPPED,
                "Admin",
                1_000L,
                6_000L,
                1,
                0,
                0,
                100,
                60_000L,
                1_000_000L,
                false,
                timing,
                new SessionConflictSummary(0, 0, "No conflicts detected.", Map.of(), List.of(), List.of()));
        TraceReportEnvironment environment = new TraceReportEnvironment(
                "Paper test", "25", "Paper 26.2", "1.0.0", "Paper 26.2", Map.of("EventLens", "1.0.0"), 3L);
        return new TraceReportDocument(
                ExportLimits.REPORT_VERSION,
                ExportRedactionMode.SHARE_SAFE,
                environment,
                summary,
                TraceFilter.Builder.unrestricted().build(),
                List.of(),
                List.of(dispatch));
    }
}
