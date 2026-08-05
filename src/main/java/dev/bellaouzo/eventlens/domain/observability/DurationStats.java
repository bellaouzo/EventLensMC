package dev.bellaouzo.eventlens.domain.observability;

public record DurationStats(
        int count, long minNanos, long maxNanos, long averageNanos, long p50Nanos, long p95Nanos, long p99Nanos) {

    public static DurationStats empty() {
        return new DurationStats(0, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    public String formatAverageMillis() {
        return formatMillis(averageNanos);
    }

    public String formatP95Millis() {
        return formatMillis(p95Nanos);
    }

    public String formatMaxMillis() {
        return formatMillis(maxNanos);
    }

    public static String formatMillis(long nanos) {
        if (nanos <= 0L) {
            return "0.00ms";
        }
        double millis = nanos / 1_000_000.0;
        return String.format(java.util.Locale.ROOT, "%.2fms", millis);
    }
}
