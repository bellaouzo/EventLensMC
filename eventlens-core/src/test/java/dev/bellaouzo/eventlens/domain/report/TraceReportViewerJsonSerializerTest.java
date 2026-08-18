package dev.bellaouzo.eventlens.domain.report;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationCapabilities;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TraceReportViewerJsonSerializerTest {

    @Test
    void omitsSnapshotsAndKeepsViewerFields() {
        String json = TraceReportViewerJsonSerializer.serialize(sampleDocument());

        assertTrue(json.contains("\n  "));
        assertTrue(json.contains("\"sessionId\": \"abc12345\""));
        assertTrue(json.contains("\"blockMaterial\": \"SHORT_GRASS\""));
        assertTrue(json.contains("\"playerName\": \"Steve\""));
        assertTrue(json.contains("onBreakNormal"));
        assertFalse(json.contains("snapshotBefore"));
        assertFalse(json.contains("snapshotAfter"));
        assertFalse(json.contains("priorityCheckpoints"));
        assertFalse(json.contains("PaperTraceHookManager"));
        assertFalse(json.contains("player.uuid"));
        assertTrue(json.indexOf('\n') > 0);
        assertTrue(json.split("\n").length < 80);
    }

    private static TraceReportDocument sampleDocument() {
        EventSnapshot snapshot = new EventSnapshot(
                "org.bukkit.event.block.BlockBreakEvent",
                "LOWEST",
                1_000L,
                List.of(
                        new SnapshotField("player.name", new SnapshotValue.Present("string", "Steve")),
                        new SnapshotField("block.type", new SnapshotValue.Present("string", "SHORT_GRASS")),
                        new SnapshotField(
                                "player.uuid",
                                new SnapshotValue.Present("string", "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))));
        TraceDispatchRecord dispatch = new TraceDispatchRecord(
                1L,
                1_000L,
                111_000L,
                2_000_000L,
                300_000L,
                "org.bukkit.event.block.BlockBreakEvent",
                true,
                true,
                false,
                false,
                Optional.empty(),
                Optional.of("world"),
                Optional.of(184),
                Optional.of(64),
                Optional.of(178),
                snapshot,
                snapshot,
                List.of(snapshot, snapshot),
                List.of(
                        new TraceListenerSnapshot(
                                1,
                                "EventLens",
                                "dev.bellaouzo.eventlens.paper.PaperTraceHookManager$1",
                                "observe",
                                "LOWEST",
                                false),
                        new TraceListenerSnapshot(
                                2,
                                "EventLensTestTarget",
                                "dev.bellaouzo.eventlens.testkit.TestKitListeners",
                                "onBreakNormal",
                                "NORMAL",
                                false)),
                List.of(),
                Set.of());
        SessionTimingSummary timing = new SessionTimingSummary(
                new DurationStats(1, 1_000L, 2_000L, 1_500L, 1_500L, 2_000L, 2_000L),
                new DurationStats(1, 100L, 200L, 150L, 150L, 200L, 200L),
                List.of(),
                List.of(),
                List.of(),
                0,
                Set.of(),
                1_000_000L,
                true);
        TraceSessionSummary summary = new TraceSessionSummary(
                "abc12345",
                "org.bukkit.event.block.BlockBreakEvent",
                TraceSessionState.STOPPED,
                "Admin",
                1_000L,
                2_000L,
                1,
                0,
                0,
                128,
                60_000L,
                1_000_000L,
                false,
                timing,
                SessionConflictSummary.empty(),
                0);
        return new TraceReportDocument(
                ExportLimits.REPORT_VERSION,
                ExportRedactionMode.SHARE_SAFE,
                new TraceReportEnvironment(
                        "Paper test", "25", "Paper 26.2", "1.2.3", "Paper 26.2", Map.of("EventLens", "1.2.3"), 3L),
                new TraceReportInstrumentation(
                        InstrumentationMode.PRECISE, true, 2, true, true, InstrumentationCapabilities.precise()),
                summary,
                Optional.of(timing),
                TraceFilter.Builder.unrestricted().build(),
                List.of(),
                List.of(dispatch));
    }
}
