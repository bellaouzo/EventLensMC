package dev.bellaouzo.eventlens.command.trace;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TraceStartTabCompleterTest {

    @Test
    void completesSecondEventAfterComma() {
        List<String> suggestions = TraceStartTabCompleter.completeEventQuery(
                List.of("PlayerInteractEvent", "BlockBreakEvent", "BlockPlaceEvent"), "PlayerInteractEvent,");

        assertTrue(suggestions.contains("PlayerInteractEvent,BlockBreakEvent"));
        assertTrue(suggestions.contains("PlayerInteractEvent,BlockPlaceEvent"));
        assertTrue(suggestions.stream().noneMatch(value -> value.equals("PlayerInteractEvent,PlayerInteractEvent")));
    }

    @Test
    void filtersFragmentAfterComma() {
        List<String> suggestions = TraceStartTabCompleter.completeEventQuery(
                List.of("PlayerInteractEvent", "BlockBreakEvent", "BlockPlaceEvent"), "PlayerInteractEvent,BlockB");

        assertTrue(suggestions.contains("PlayerInteractEvent,BlockBreakEvent"));
        assertTrue(suggestions.stream().noneMatch(value -> value.contains("BlockPlaceEvent")));
    }
}
