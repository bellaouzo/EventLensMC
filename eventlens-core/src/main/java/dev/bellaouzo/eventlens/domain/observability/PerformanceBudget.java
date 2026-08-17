package dev.bellaouzo.eventlens.domain.observability;

public final class PerformanceBudget {

    public static final long TARGET_AVERAGE_NANOS = 250_000L;
    public static final long TARGET_P95_NANOS = 750_000L;
    public static final long THROTTLE_P95_NANOS = 1_000_000L;
    public static final long AUTO_STOP_P95_NANOS = 2_500_000L;
    public static final long EMERGENCY_STOP_NANOS = 10_000_000L;
    public static final int WINDOW_SIZE = 200;
    public static final int AUTO_STOP_CONSECUTIVE_WINDOWS = 3;
    public static final long DEFAULT_SLOW_THRESHOLD_NANOS = 1_000_000L;
    public static final long MAIN_THREAD_BLOCK_NANOS = 5_000_000L;
    public static final double FREQUENT_LISTENER_MULTIPLIER = 3.0;

    private PerformanceBudget() {}
}
