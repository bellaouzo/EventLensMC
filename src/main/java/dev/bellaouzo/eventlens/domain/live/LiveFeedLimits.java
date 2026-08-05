package dev.bellaouzo.eventlens.domain.live;

public final class LiveFeedLimits {

    public static final int MAX_SUBSCRIBERS = 8;
    public static final int MAX_LINES_PER_TICK = 8;
    public static final long DEFAULT_AGGREGATE_WINDOW_MILLIS = 3_000L;
    public static final long DEFAULT_BURST_WINDOW_MILLIS = 5_000L;
    public static final int DEFAULT_BURST_THRESHOLD = 50;
    public static final long BURST_ALERT_COOLDOWN_MILLIS = 10_000L;
    public static final long STATUS_UPDATE_INTERVAL_MILLIS = 1_000L;

    private LiveFeedLimits() {}
}
