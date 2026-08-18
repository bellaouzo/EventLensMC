package dev.bellaouzo.eventlens.domain.narrative;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransition;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NarrativeBuilderTest {

    @Test
    void summarisesCancelAndException() {
        ListenerTimingRecord timing = new ListenerTimingRecord(
                1,
                "GriefPlugin",
                "Listener",
                "onBreak",
                "HIGH",
                1_000_000L,
                true,
                false,
                false,
                Optional.empty(),
                true,
                Optional.of("RuntimeException"),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.of(new CancellationTransition(false, true, CancellationTransitionKind.BECAME_CANCELLED)));
        TraceDispatchRecord dispatch = new TraceDispatchRecord(
                1L,
                0L,
                0L,
                1_000_000L,
                0L,
                "org.bukkit.event.block.BlockBreakEvent",
                true,
                true,
                false,
                true,
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.of(1),
                Optional.of(2),
                Optional.of(3),
                emptySnapshot(),
                emptySnapshot(),
                List.of(),
                List.of(new TraceListenerSnapshot(1, "GriefPlugin", "Listener", "onBreak", "HIGH", false)),
                List.of(timing),
                Set.of(TracePartialReason.AGENT_ABSENT));
        DispatchNarrative narrative = NarrativeBuilder.dispatch(dispatch);
        assertTrue(narrative.summary().contains("GriefPlugin"));
        assertTrue(narrative.summary().contains("threw"));
        assertTrue(narrative.cancelledBy().isPresent());
    }

    private static EventSnapshot emptySnapshot() {
        return new EventSnapshot("org.bukkit.event.block.BlockBreakEvent", "LOWEST", 0L, 0L, List.of());
    }
}
