package dev.bellaouzo.eventlens.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CommandTextTest {

    @Test
    void matchesEventNamesByContainedWord() {
        List<String> values = List.of("BlockBreakEvent", "EntityExplodeEvent", "BlockExplodeEvent", "PlayerJoinEvent");

        List<String> matches = CommandText.filterPrefix(values, "explode");

        assertEquals(List.of("EntityExplodeEvent", "BlockExplodeEvent"), matches);
    }

    @Test
    void ranksPrefixMatchesAheadOfContains() {
        List<String> values = List.of("EntityDamageEvent", "EntityDamageByEntityEvent", "Damage");

        List<String> matches = CommandText.filterPrefix(values, "Damage");

        assertEquals("Damage", matches.getFirst());
        assertTrue(matches.contains("EntityDamageEvent"));
        assertTrue(matches.contains("EntityDamageByEntityEvent"));
    }
}
