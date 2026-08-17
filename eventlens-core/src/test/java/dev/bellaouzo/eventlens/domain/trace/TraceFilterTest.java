package dev.bellaouzo.eventlens.domain.trace;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraceFilterTest {

    @Test
    void matchesPluginPlayerWorldRegionAndCancellation() {
        TraceFilter filter = new TraceFilter.Builder()
                .pluginName("Essentials")
                .playerName("Steve")
                .worldName("world")
                .region(new TraceRegion(0, 0, 100, 100))
                .cancellationFilter(TraceCancellationFilter.CANCELLED)
                .build();

        EventFilterContext matching = new EventFilterContext(
                "org.bukkit.event.player.PlayerJoinEvent",
                true,
                true,
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.of(10),
                Optional.of(64),
                Optional.of(10),
                List.of("Essentials", "EventLens"));

        assertTrue(filter.matches(matching));
        assertFalse(filter.matches(new EventFilterContext(
                "org.bukkit.event.player.PlayerJoinEvent",
                true,
                false,
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.of(10),
                Optional.of(64),
                Optional.of(10),
                List.of("Essentials"))));
    }
}
