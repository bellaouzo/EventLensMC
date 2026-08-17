package dev.bellaouzo.eventlens.domain.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransition;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraceReportRedactorTest {

    @Test
    void shareSafeRedactionRemovesPlayerAndWorld() {
        TraceReportDocument document = sampleDocument(ExportRedactionMode.FULL);
        TraceReportDocument redacted = TraceReportRedactor.apply(document, ExportRedactionMode.SHARE_SAFE);

        assertTrue(redacted.dispatches().getFirst().playerName().isEmpty());
        assertTrue(redacted.dispatches().getFirst().worldName().isEmpty());
        assertTrue(redacted.dispatches().getFirst().blockX().isEmpty());
        assertTrue(redacted.filter().playerName().isEmpty());
        ListenerTimingRecord timing =
                redacted.dispatches().getFirst().listenerTimings().getFirst();
        assertTrue(timing.exceptionType().isPresent());
        assertEquals(1, timing.propertyChanges().size());
        assertTrue(timing.stackTrace().isEmpty());
        assertEqualsRedactionMode(ExportRedactionMode.SHARE_SAFE, redacted.redactionMode());
    }

    @Test
    void jsonExportIsPrettyPrinted() {
        TraceReportDocument document = sampleDocument(ExportRedactionMode.SHARE_SAFE);
        String json = TraceReportJsonSerializer.serialize(document);
        assertTrue(json.contains("\n"));
        assertTrue(json.contains("  \"reportVersion\""));
    }

    @Test
    void compactReportIncludesConflictSummary() {
        TraceReportDocument document =
                TraceReportRedactor.apply(sampleDocument(ExportRedactionMode.FULL), ExportRedactionMode.SHARE_SAFE);
        String compact = TraceReportTextSerializer.compact(document);
        assertTrue(compact.contains("Conflicts:"));
        assertFalse(compact.contains("Steve"));
    }

    private static void assertEqualsRedactionMode(ExportRedactionMode expected, ExportRedactionMode actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }

    private static TraceReportDocument sampleDocument(ExportRedactionMode mode) {
        EventSnapshot snapshot = new EventSnapshot(
                "org.example.TestEvent",
                "LOWEST",
                1L,
                List.of(new SnapshotField("player", new SnapshotValue.Present("String", "Steve"))));
        ListenerTimingRecord timing = new ListenerTimingRecord(
                1,
                "PluginA",
                "dev.example.Listener",
                "onEvent",
                "LOW",
                1_000_000L,
                true,
                false,
                false,
                Optional.of("java.lang.RuntimeException: test"),
                true,
                Optional.of("java.lang.RuntimeException"),
                Optional.of(snapshot),
                Optional.of(snapshot),
                List.of(new PropertyChange(
                        "cancelled",
                        new SnapshotValue.Present("boolean", "false"),
                        new SnapshotValue.Present("boolean", "true"))),
                Optional.of(new CancellationTransition(false, true, CancellationTransitionKind.BECAME_CANCELLED)));
        TraceDispatchRecord dispatch = new TraceDispatchRecord(
                1L,
                1L,
                1L,
                1L,
                0L,
                "org.example.TestEvent",
                true,
                false,
                false,
                false,
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.of(10),
                Optional.empty(),
                Optional.empty(),
                snapshot,
                snapshot,
                List.of(snapshot),
                List.of(),
                List.of(timing),
                java.util.EnumSet.noneOf(dev.bellaouzo.eventlens.domain.trace.TracePartialReason.class));
        TraceSessionSummary summary = new TraceSessionSummary(
                "abc12345",
                "org.example.TestEvent",
                TraceSessionState.STOPPED,
                "Admin",
                1_000L,
                2_000L,
                1,
                0,
                0,
                100,
                60_000L,
                1_000_000L,
                false,
                null,
                new SessionConflictSummary(1, 0, "No conflicts detected.", Map.of(), List.of(), List.of()),
                0);
        TraceReportEnvironment environment = new TraceReportEnvironment(
                "Paper test", "25", "Paper 26.2", "1.0.0", "Paper 26.2", Map.of("EventLens", "1.0.0"), 3L);
        TraceFilter filter =
                TraceFilter.Builder.unrestricted().playerName("Steve").build();
        return new TraceReportDocument(
                ExportLimits.REPORT_VERSION,
                mode,
                environment,
                summary,
                filter,
                List.of("Incomplete: Java agent absent; per-listener timing unavailable."),
                List.of(dispatch));
    }
}
