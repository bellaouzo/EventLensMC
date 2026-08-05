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
        SessionConflictSummary conflictSummary) {

    public TraceSessionSummary {
        conflictSummary = conflictSummary == null ? SessionConflictSummary.empty() : conflictSummary;
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
                SessionConflictSummary.empty());
    }
}
