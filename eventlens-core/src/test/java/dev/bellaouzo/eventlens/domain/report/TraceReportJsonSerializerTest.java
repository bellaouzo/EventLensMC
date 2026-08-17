package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransition;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationCapabilities;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.observability.RankedListenerTiming;
import dev.bellaouzo.eventlens.domain.observability.RankedPluginTiming;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerIdentity;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TraceReportJsonSerializerTest {

    @Test
    void serializesPhase2ReportShape() throws IOException {
        TraceReportDocument document = sampleDocument();
        String actual = TraceReportJsonSerializer.serialize(document);
        for (String line : fixture("trace-report-v2-golden.json").lines().toList()) {
            String expectedFragment = line.trim();
            if (expectedFragment.isEmpty()) {
                continue;
            }
            Assertions.assertTrue(
                    actual.contains(expectedFragment),
                    "Missing expected fragment from golden fixture: " + expectedFragment);
        }
    }

    private static TraceReportDocument sampleDocument() {
        EventSnapshot dispatchBefore = new EventSnapshot(
                "org.bukkit.event.player.PlayerInteractEvent",
                "DISPATCH_START",
                1_000L,
                111_000L,
                List.of(
                        new SnapshotField("player", new SnapshotValue.Present("String", "Steve")),
                        new SnapshotField("item", new SnapshotValue.Truncated("{...}", "entry-limit"))));
        EventSnapshot listenerBefore = new EventSnapshot(
                "org.bukkit.event.player.PlayerInteractEvent",
                "LOW",
                1_001L,
                111_100L,
                List.of(new SnapshotField("cancelled", new SnapshotValue.Present("boolean", "false"))));
        EventSnapshot listenerAfter = new EventSnapshot(
                "org.bukkit.event.player.PlayerInteractEvent",
                "LOW",
                1_002L,
                111_200L,
                List.of(new SnapshotField("cancelled", new SnapshotValue.Present("boolean", "true"))));
        ListenerTimingRecord timing = new ListenerTimingRecord(
                1,
                "ProtectionPlugin",
                "dev.example.ProtectionListener",
                "onInteract",
                "LOW",
                1_500_000L,
                true,
                false,
                true,
                Optional.of("java.lang.RuntimeException: boom"),
                true,
                Optional.of("java.lang.RuntimeException"),
                Optional.of(listenerBefore),
                Optional.of(listenerAfter),
                List.of(new PropertyChange(
                        "cancelled",
                        new SnapshotValue.Present("boolean", "false"),
                        new SnapshotValue.Present("boolean", "true"))),
                Optional.of(new CancellationTransition(false, true, CancellationTransitionKind.BECAME_CANCELLED)));
        TraceDispatchRecord dispatch = new TraceDispatchRecord(
                5L,
                1_000L,
                111_000L,
                2_500_000L,
                350_000L,
                "org.bukkit.event.player.PlayerInteractEvent",
                true,
                true,
                false,
                true,
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.of(12),
                Optional.of(64),
                Optional.of(-8),
                dispatchBefore,
                listenerAfter,
                List.of(listenerBefore, listenerAfter),
                List.of(),
                List.of(timing),
                EnumSet.of(TracePartialReason.LISTENER_SNAPSHOTS_UNAVAILABLE));
        SessionTimingSummary timingSummary = new SessionTimingSummary(
                new DurationStats(4, 1_000_000L, 4_000_000L, 2_250_000L, 2_000_000L, 3_800_000L, 4_000_000L),
                new DurationStats(4, 100_000L, 400_000L, 210_000L, 200_000L, 360_000L, 400_000L),
                List.of(new RankedListenerTiming(
                        new ListenerIdentity("ProtectionPlugin", "dev.example.ProtectionListener", "onInteract"),
                        new DurationStats(4, 900_000L, 2_400_000L, 1_350_000L, 1_300_000L, 2_100_000L, 2_400_000L),
                        4,
                        true,
                        false)),
                List.of(new RankedPluginTiming(
                        "ProtectionPlugin",
                        new DurationStats(4, 900_000L, 2_400_000L, 1_350_000L, 1_300_000L, 2_100_000L, 2_400_000L),
                        4)),
                List.of("ProtectionPlugin/ProtectionListener.onInteract invoked 4 times"),
                1,
                Set.of(TracePartialReason.AGENT_ABSENT),
                1_000_000L,
                false);
        TraceSessionSummary summary = new TraceSessionSummary(
                "abc12345",
                "org.bukkit.event.player.PlayerInteractEvent",
                TraceSessionState.STOPPED,
                "Admin",
                1_000L,
                2_000L,
                1,
                0,
                1,
                128,
                60_000L,
                1_000_000L,
                false,
                timingSummary,
                new SessionConflictSummary(1, 0, "No conflicts detected.", Map.of(), List.of(), List.of()));
        TraceReportEnvironment environment = new TraceReportEnvironment(
                "Paper test",
                "25",
                "Paper 26.2",
                "0.1-SNAPSHOT",
                "Paper 26.2",
                Map.of("EventLens", "0.1-SNAPSHOT"),
                3L);
        TraceReportInstrumentation instrumentation = new TraceReportInstrumentation(
                InstrumentationMode.DEGRADED, true, 1, false, true, InstrumentationCapabilities.degraded(false));
        return new TraceReportDocument(
                ExportLimits.REPORT_VERSION,
                ExportRedactionMode.FULL,
                environment,
                instrumentation,
                summary,
                Optional.of(timingSummary),
                TraceFilter.Builder.unrestricted().playerName("Steve").build(),
                List.of("Incomplete: Java agent protocol incompatible with plugin build."),
                List.of(dispatch));
    }

    private static String fixture(String name) throws IOException {
        String path = "/dev/bellaouzo/eventlens/domain/report/" + name;
        InputStream stream = TraceReportJsonSerializerTest.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Missing fixture " + path);
        }
        try (stream) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
