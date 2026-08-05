package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.live.LiveFeedDisplayMode;
import dev.bellaouzo.eventlens.domain.live.LiveFeedLimits;

public record LiveFeedConfig(
        long aggregateWindowMillis,
        long burstWindowMillis,
        int burstThreshold,
        int maxLinesPerTick,
        LiveFeedDisplayMode defaultStatusDisplay) {

    public static LiveFeedConfig defaults() {
        return new LiveFeedConfig(
                LiveFeedLimits.DEFAULT_AGGREGATE_WINDOW_MILLIS,
                LiveFeedLimits.DEFAULT_BURST_WINDOW_MILLIS,
                LiveFeedLimits.DEFAULT_BURST_THRESHOLD,
                LiveFeedLimits.MAX_LINES_PER_TICK,
                LiveFeedDisplayMode.ACTION_BAR);
    }
}
