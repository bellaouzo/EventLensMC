package dev.bellaouzo.eventlens.domain.live;

import java.util.EnumSet;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public record LiveFeedSettings(
        @NonNull EnumSet<LiveFeedChannel> channels,
        @NonNull LiveFeedDisplayMode displayMode,
        boolean paused,
        @NonNull Optional<String> pluginFilter,
        long slowThresholdNanos,
        int burstThreshold,
        long burstWindowMillis,
        long aggregateWindowMillis) {

    public static LiveFeedSettings defaults(long slowThresholdNanos) {
        return new LiveFeedSettings(
                EnumSet.allOf(LiveFeedChannel.class),
                LiveFeedDisplayMode.CHAT,
                false,
                Optional.empty(),
                slowThresholdNanos,
                LiveFeedLimits.DEFAULT_BURST_THRESHOLD,
                LiveFeedLimits.DEFAULT_BURST_WINDOW_MILLIS,
                LiveFeedLimits.DEFAULT_AGGREGATE_WINDOW_MILLIS);
    }

    public LiveFeedSettings withPaused(boolean newPaused) {
        return new LiveFeedSettings(
                channels,
                displayMode,
                newPaused,
                pluginFilter,
                slowThresholdNanos,
                burstThreshold,
                burstWindowMillis,
                aggregateWindowMillis);
    }

    public boolean acceptsChannel(LiveFeedChannel channel) {
        return channels.contains(channel);
    }
}
