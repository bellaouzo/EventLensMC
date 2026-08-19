package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EventLensDashboardConfigTest {

    @Test
    void loopbackWithoutTokenCanBind() {
        EventLensDashboardConfig config = EventLensDashboardConfig.defaults();
        assertTrue(config.isLoopbackOnly());
        assertFalse(config.hasToken());
        assertTrue(config.canBind());
        assertTrue(config.tokenMatches(null));
    }

    @Test
    void remoteBindRequiresLongToken() {
        EventLensDashboardConfig missing = new EventLensDashboardConfig(true, 8765, "0.0.0.0", "");
        EventLensDashboardConfig tooShort = new EventLensDashboardConfig(true, 8765, "0.0.0.0", "short-token");
        EventLensDashboardConfig allowed = new EventLensDashboardConfig(true, 8765, "0.0.0.0", "hosted-token-1");

        assertFalse(missing.canBind());
        assertFalse(tooShort.canBind());
        assertTrue(allowed.canBind());
        assertTrue(allowed.tokenMatches("hosted-token-1"));
        assertFalse(allowed.tokenMatches("wrong-token-12"));
        assertFalse(allowed.toString().contains("hosted-token-1"));
    }
}
