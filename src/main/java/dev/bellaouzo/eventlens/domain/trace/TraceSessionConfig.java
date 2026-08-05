package dev.bellaouzo.eventlens.domain.trace;

import java.util.Optional;

public record TraceSessionConfig(
        String eventClassName,
        TraceFilter filter,
        Optional<Long> maxDurationMillis,
        Optional<Integer> maxEventCount,
        long slowThresholdNanos,
        boolean captureStacks) {

    public TraceSessionConfig(
            String eventClassName,
            TraceFilter filter,
            Optional<Long> maxDurationMillis,
            Optional<Integer> maxEventCount) {
        this(
                eventClassName,
                filter,
                maxDurationMillis,
                maxEventCount,
                dev.bellaouzo.eventlens.domain.observability.PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS,
                false);
    }

    public int effectiveMaxEventCount() {
        return maxEventCount.orElse(TraceLimits.MAX_RECORDS_PER_SESSION);
    }

    public long effectiveMaxDurationMillis() {
        return maxDurationMillis.orElse(TraceLimits.DEFAULT_MAX_DURATION_MILLIS);
    }
}
