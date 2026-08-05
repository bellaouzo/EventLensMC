package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.bellaouzo.eventlens.domain.observability.RankedListenerTiming;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SessionTimingAnalyzerTest {

    @Test
    void ranksSlowestListenersAndPlugins() {
        TraceDispatchRecord dispatch = dispatchWithTimings(List.of(
                timing("FastPlugin", "FastListener", "fast", 100_000L),
                timing("SlowPlugin", "SlowListener", "slow", 2_000_000L)));

        SessionTimingSummary summary = SessionTimingAnalyzer.analyze(List.of(dispatch), 0, 1_000_000L, true);

        assertEquals(1, summary.dispatchStats().count());
        assertFalse(summary.slowestListeners().isEmpty());
        RankedListenerTiming slowest = summary.slowestListeners().getFirst();
        assertEquals("SlowPlugin", slowest.identity().pluginName());
        assertEquals("SlowPlugin", summary.slowestPlugins().getFirst().pluginName());
    }

    private static TraceDispatchRecord dispatchWithTimings(List<ListenerTimingRecord> timings) {
        EventSnapshot snapshot = new EventSnapshot("org.example.TestEvent", "LOWEST", 1_000L, List.of());
        return new TraceDispatchRecord(
                1L,
                1_000L,
                1_000_000_000L,
                3_000_000L,
                50_000L,
                "org.example.TestEvent",
                true,
                false,
                false,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                snapshot,
                snapshot,
                List.of(snapshot),
                List.of(),
                timings,
                EnumSet.noneOf(TracePartialReason.class));
    }

    private static ListenerTimingRecord timing(String plugin, String listenerClass, String method, long durationNanos) {
        return ListenerTimingRecord.timingOnly(
                1,
                plugin,
                listenerClass,
                method,
                "NORMAL",
                durationNanos,
                true,
                durationNanos >= 5_000_000L,
                durationNanos >= 1_000_000L,
                Optional.empty(),
                false,
                Optional.empty());
    }
}
