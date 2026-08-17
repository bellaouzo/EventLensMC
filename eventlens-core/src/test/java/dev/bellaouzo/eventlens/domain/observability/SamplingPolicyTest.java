package dev.bellaouzo.eventlens.domain.observability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import org.junit.jupiter.api.Test;

class SamplingPolicyTest {

    @Test
    void hotEventRequiresNarrowingFilter() {
        SamplingPolicy policy = new SamplingPolicy();

        assertFalse(policy.accepts("org.bukkit.event.player.PlayerMoveEvent", TraceFilter.unrestricted()));
    }

    @Test
    void hotEventAcceptsWithPlayerFilter() {
        SamplingPolicy policy = new SamplingPolicy();
        TraceFilter filter =
                TraceFilter.Builder.unrestricted().playerName("Steve").build();

        boolean acceptedOnce = false;
        for (int index = 0; index < 40; index++) {
            if (policy.accepts("org.bukkit.event.player.PlayerMoveEvent", filter)) {
                acceptedOnce = true;
                break;
            }
        }

        assertTrue(acceptedOnce);
    }

    @Test
    void normalEventAlwaysAccepted() {
        SamplingPolicy policy = new SamplingPolicy();

        assertTrue(policy.accepts("org.bukkit.event.player.PlayerInteractEvent", TraceFilter.unrestricted()));
    }
}
