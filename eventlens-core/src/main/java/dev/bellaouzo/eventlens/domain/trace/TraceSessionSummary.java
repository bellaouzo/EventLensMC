package dev.bellaouzo.eventlens.domain.trace;

import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import org.jspecify.annotations.NonNull;

public record TraceSessionSummary(
        @NonNull String sessionId,
        @NonNull String eventClassName,
        @NonNull TraceSessionState state,
        @NonNull String ownerName,
        long startedAtMillis,
        long lastActivityAtMillis,
        int capturedEvents,
        int droppedEvents,
        int sampledOutEvents,
        int maxEventCount,
        long maxDurationMillis,
        long slowThresholdNanos,
        boolean captureStacks,
        SessionTimingSummary timingSummary,
        SessionConflictSummary conflictSummary,
        int restartCount,
        java.util.List<String> eventClassNames) {

    public TraceSessionSummary {
        conflictSummary = conflictSummary == null ? SessionConflictSummary.empty() : conflictSummary;
        restartCount = Math.max(0, restartCount);
        eventClassNames = eventClassNames == null || eventClassNames.isEmpty()
                ? java.util.List.of(eventClassName)
                : java.util.List.copyOf(eventClassNames);
    }

    public TraceSessionSummary(
            String sessionId,
            String eventClassName,
            TraceSessionState state,
            String ownerName,
            long startedAtMillis,
            long lastActivityAtMillis,
            int capturedEvents,
            int droppedEvents,
            int sampledOutEvents,
            int maxEventCount,
            long maxDurationMillis,
            long slowThresholdNanos,
            boolean captureStacks,
            SessionTimingSummary timingSummary,
            SessionConflictSummary conflictSummary,
            int restartCount) {
        this(
                sessionId,
                eventClassName,
                state,
                ownerName,
                startedAtMillis,
                lastActivityAtMillis,
                capturedEvents,
                droppedEvents,
                sampledOutEvents,
                maxEventCount,
                maxDurationMillis,
                slowThresholdNanos,
                captureStacks,
                timingSummary,
                conflictSummary,
                restartCount,
                java.util.List.of(eventClassName));
    }

    public boolean restarted() {
        return restartCount > 0;
    }

    public String restartBadge() {
        if (restartCount <= 0) {
            return "";
        }
        return restartCount == 1 ? "RESTARTED" : "RESTARTED ×" + restartCount;
    }

    public TraceSessionSummary(
            String sessionId,
            String eventClassName,
            TraceSessionState state,
            String ownerName,
            long startedAtMillis,
            long lastActivityAtMillis,
            int capturedEvents,
            int droppedEvents,
            int maxEventCount,
            long maxDurationMillis) {
        this(
                sessionId,
                eventClassName,
                state,
                ownerName,
                startedAtMillis,
                lastActivityAtMillis,
                capturedEvents,
                droppedEvents,
                0,
                maxEventCount,
                maxDurationMillis,
                dev.bellaouzo.eventlens.domain.observability.PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS,
                false,
                null,
                SessionConflictSummary.empty(),
                0);
    }
}
