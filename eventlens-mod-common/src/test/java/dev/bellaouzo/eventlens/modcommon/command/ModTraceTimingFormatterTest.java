package dev.bellaouzo.eventlens.modcommon.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransition;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ModTraceTimingFormatterTest {

    @Test
    void listsModHandlersAndCancelChange() {
        ListenerTimingRecord jei = new ListenerTimingRecord(
                1,
                "jei",
                "jei.ChatHook",
                "onChat",
                "NORMAL",
                120_000L,
                true,
                false,
                false,
                Optional.empty(),
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.of(new CancellationTransition(false, true, CancellationTransitionKind.BECAME_CANCELLED)));
        ListenerTimingRecord chatPlus = ListenerTimingRecord.timingOnly(
                2,
                "chatplus",
                "chatplus.Filter",
                "onChat",
                "HIGH",
                80_000L,
                true,
                false,
                false,
                Optional.empty(),
                false,
                Optional.empty());
        TraceDispatchRecord record = dispatch(List.of(jei, chatPlus));
        List<ModChatLine> lines = ModTraceTimingFormatter.listenerLines(record, true);
        assertTrue(lines.stream().anyMatch(line -> contains(line, "jei")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "chatplus")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "cancelled")));
    }

    private static boolean contains(ModChatLine line, String text) {
        return line.spans().stream().anyMatch(span -> span.text().contains(text));
    }

    private static TraceDispatchRecord dispatch(List<ListenerTimingRecord> timings) {
        EventSnapshot snapshot = new EventSnapshot("chat", "DISPATCH", 1L, List.of());
        return new TraceDispatchRecord(
                1L,
                1L,
                1L,
                1L,
                0L,
                "chat",
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
                List.of(),
                List.of(),
                timings,
                EnumSet.noneOf(TracePartialReason.class));
    }
}
