package dev.bellaouzo.eventlens.domain.report;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DashboardLiveUpdateSerializerTest {

    @Test
    void serializesCompactDispatchUpdate() {
        TraceSessionSummary summary = new TraceSessionSummary(
                "abc12345",
                "org.bukkit.event.player.PlayerInteractEvent",
                TraceSessionState.ACTIVE,
                "Tester",
                1_000L,
                2_500L,
                3,
                0,
                4096,
                300_000L);

        TraceDispatchRecord dispatch = new TraceDispatchRecord(
                3L,
                2_000L,
                0L,
                1_500_000L,
                120_000L,
                "org.bukkit.event.player.PlayerInteractEvent",
                true,
                true,
                false,
                false,
                Optional.of("Tester"),
                Optional.of("world"),
                Optional.of(1),
                Optional.of(2),
                Optional.of(3),
                null,
                null,
                List.of(),
                List.of(),
                List.of(ListenerTimingRecord.timingOnly(
                        1,
                        "WorldGuard",
                        "com.example.Listener",
                        "onInteract",
                        "NORMAL",
                        900_000L,
                        true,
                        false,
                        true,
                        Optional.empty(),
                        false,
                        Optional.empty())),
                null);

        String json = DashboardLiveUpdateSerializer.serializeDispatchUpdate(summary, dispatch);

        assertTrue(json.contains("abc12345"));
        assertTrue(json.contains("capturedEvents"));
        assertTrue(json.contains("3"));
        assertTrue(json.contains("WorldGuard"));
        assertTrue(json.contains("exceedsSlowThreshold"));
        assertTrue(!json.contains("snapshotBefore"));
    }
}
