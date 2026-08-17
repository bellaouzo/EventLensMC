package dev.bellaouzo.eventlens.domain.trace;

public final class TraceLimits {

    public static final int MAX_CONCURRENT_SESSIONS = 4;
    public static final int MAX_RECORDS_PER_SESSION = 4_096;
    public static final int MAX_LISTENERS_PER_DISPATCH = 256;
    public static final long DEFAULT_MAX_DURATION_MILLIS = 300_000L;
    public static final long ABANDONED_SESSION_MILLIS = 1_800_000L;

    private TraceLimits() {}
}
