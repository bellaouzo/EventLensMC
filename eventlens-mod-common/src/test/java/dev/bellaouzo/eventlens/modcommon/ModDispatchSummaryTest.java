package dev.bellaouzo.eventlens.modcommon;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ModDispatchSummaryTest {

    @Test
    void listLineIncludesMessagePlayerAndHandlers() {
        String line = ModDispatchSummary.listLine(dispatch(
                false,
                Optional.of("Dev"),
                List.of(ModSnapshotFields.text("message", "hello there"), ModSnapshotFields.bool("cancelled", false)),
                List.of(handler("jei"), handler("chatplus"))));
        assertTrue(line.startsWith("#1  1.67 ms"));
        assertTrue(line.contains("Dev"));
        assertTrue(line.contains("hello there"));
        assertTrue(line.contains("2 handlers"));
        assertFalse(line.contains("cancelled"));
    }

    @Test
    void listLineMarksCancelledAndPrefersMessageOverCoords() {
        String line = ModDispatchSummary.listLine(dispatch(
                true,
                Optional.empty(),
                List.of(
                        ModSnapshotFields.number("x", 12),
                        ModSnapshotFields.number("y", 64),
                        ModSnapshotFields.text("message", "hi")),
                List.of()));
        assertTrue(line.contains("cancelled"));
        assertTrue(line.contains("hi"));
        assertFalse(line.contains("x 12"));
    }

    private static ListenerTimingRecord handler(String modId) {
        return ListenerTimingRecord.timingOnly(
                1, modId, modId + ".Hook", "onEvent", "NORMAL", 80_000L, true, false, false, Optional.empty(), false, Optional.empty());
    }

    private static TraceDispatchRecord dispatch(
            boolean cancelled,
            Optional<String> player,
            List<SnapshotField> fields,
            List<ListenerTimingRecord> timings) {
        EventSnapshot snapshot = new EventSnapshot("chat", "DISPATCH", 1L, fields);
        return new TraceDispatchRecord(
                1L,
                1L,
                1L,
                1_670_000L,
                0L,
                "chat",
                true,
                true,
                false,
                cancelled,
                player,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                snapshot,
                snapshot,
                List.of(),
                List.of(),
                timings,
                EnumSet.noneOf(TracePartialReason.class));
    }
}
