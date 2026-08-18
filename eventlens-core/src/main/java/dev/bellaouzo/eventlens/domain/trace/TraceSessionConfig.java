package dev.bellaouzo.eventlens.domain.trace;

import java.util.List;
import java.util.Optional;

public record TraceSessionConfig(
        List<String> eventClassNames,
        TraceFilter filter,
        Optional<Long> maxDurationMillis,
        Optional<Integer> maxEventCount,
        long slowThresholdNanos,
        boolean captureStacks) {

    public static final int MAX_EVENT_TYPES = 8;

    public TraceSessionConfig {
        if (eventClassNames == null || eventClassNames.isEmpty()) {
            throw new IllegalArgumentException("At least one event type is required.");
        }
        if (eventClassNames.size() > MAX_EVENT_TYPES) {
            throw new IllegalArgumentException("A session can trace at most " + MAX_EVENT_TYPES + " event types.");
        }
        eventClassNames = List.copyOf(eventClassNames);
    }

    public TraceSessionConfig(
            String eventClassName,
            TraceFilter filter,
            Optional<Long> maxDurationMillis,
            Optional<Integer> maxEventCount,
            long slowThresholdNanos,
            boolean captureStacks) {
        this(List.of(eventClassName), filter, maxDurationMillis, maxEventCount, slowThresholdNanos, captureStacks);
    }

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

    public String eventClassName() {
        return eventClassNames.getFirst();
    }

    public boolean acceptsEvent(String className) {
        return eventClassNames.contains(className);
    }

    public int effectiveMaxEventCount() {
        return maxEventCount.orElse(TraceLimits.MAX_RECORDS_PER_SESSION);
    }

    public long effectiveMaxDurationMillis() {
        return maxDurationMillis.orElse(TraceLimits.DEFAULT_MAX_DURATION_MILLIS);
    }
}
