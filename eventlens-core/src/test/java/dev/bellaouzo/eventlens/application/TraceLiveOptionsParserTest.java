package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.live.LiveFeedChannel;
import dev.bellaouzo.eventlens.domain.live.LiveFeedDisplayMode;
import org.junit.jupiter.api.Test;

class TraceLiveOptionsParserTest {

    @Test
    void parsesLiveFlags() {
        TraceLiveOptionsParser.ParsedLiveOptions options = TraceLiveOptionsParser.parse(
                new String[] {
                    "trace",
                    "live",
                    "abc12345",
                    "--channels",
                    "slow,exception",
                    "--display",
                    "bossbar",
                    "--filter-plugin",
                    "Vault",
                    "--threshold",
                    "2ms",
                    "--burst",
                    "40",
                    "--aggregate",
                    "5s"
                },
                LiveFeedConfig.defaults(),
                1_000_000L);

        assertEquals("abc12345", options.sessionId());
        assertEquals(LiveFeedDisplayMode.BOSS_BAR, options.settings().displayMode());
        assertTrue(options.settings().channels().contains(LiveFeedChannel.SLOW));
        assertTrue(options.settings().pluginFilter().orElse("").equalsIgnoreCase("Vault"));
        assertEquals(40, options.settings().burstThreshold());
        assertEquals(5_000L, options.settings().aggregateWindowMillis());
    }
}
