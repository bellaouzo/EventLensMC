package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.application.PerformanceBudgetController;
import dev.bellaouzo.eventlens.application.SessionConflictAnalyzer;
import dev.bellaouzo.eventlens.application.SessionTimingAnalyzer;
import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.observability.SamplingPolicy;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.trace.EventFilterContext;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

final class TraceSession {

    private final String sessionId;
    private final TraceSessionConfig config;
    private final String ownerName;
    private final long startedAtMillis;
    private final int restartCount;
    private final AtomicLong sequence = new AtomicLong(0);
    private final SamplingPolicy samplingPolicy = new SamplingPolicy();
    private final PerformanceBudgetController budgetController = new PerformanceBudgetController();

    private volatile TraceSessionState state = TraceSessionState.ACTIVE;
    private volatile long lastActivityAtMillis;
    private final List<TraceDispatchRecord> records = new ArrayList<>();
    private int droppedEvents;
    private int sampledOutEvents;
    private boolean throttledCapture;

    TraceSession(
            String sessionId, TraceSessionConfig config, String ownerName, long startedAtMillis, int restartCount) {
        this.sessionId = sessionId;
        this.config = config;
        this.ownerName = ownerName;
        this.startedAtMillis = startedAtMillis;
        this.lastActivityAtMillis = startedAtMillis;
        this.restartCount = Math.max(0, restartCount);
    }

    int getRestartCount() {
        return restartCount;
    }

    String getSessionId() {
        return sessionId;
    }

    String getEventClassName() {
        return config.eventClassName();
    }

    TraceSessionConfig getConfig() {
        return config;
    }

    String getOwnerName() {
        return ownerName;
    }

    TraceSessionState getState() {
        return state;
    }

    boolean isActive() {
        return state == TraceSessionState.ACTIVE || state == TraceSessionState.THROTTLED;
    }

    boolean isOpen() {
        return isActive() || state == TraceSessionState.PAUSED;
    }

    long getStartedAtMillis() {
        return startedAtMillis;
    }

    long getLastActivityAtMillis() {
        return lastActivityAtMillis;
    }

    boolean isThrottledCapture() {
        return throttledCapture;
    }

    List<TraceDispatchRecord> getRecordsSnapshot() {
        synchronized (records) {
            return List.copyOf(records);
        }
    }

    TraceSessionSummary toSummary(boolean agentAttached) {
        List<TraceDispatchRecord> recordsSnapshot = getRecordsSnapshot();
        SessionTimingSummary timingSummary = SessionTimingAnalyzer.analyze(
                recordsSnapshot, sampledOutEvents, config.slowThresholdNanos(), agentAttached);
        SessionConflictSummary conflictSummary =
                SessionConflictAnalyzer.analyze(recordsSnapshot, config.slowThresholdNanos());
        return new TraceSessionSummary(
                sessionId,
                config.eventClassName(),
                state,
                ownerName,
                startedAtMillis,
                lastActivityAtMillis,
                records.size(),
                droppedEvents,
                sampledOutEvents,
                config.effectiveMaxEventCount(),
                config.effectiveMaxDurationMillis(),
                config.slowThresholdNanos(),
                config.captureStacks(),
                timingSummary,
                conflictSummary,
                restartCount);
    }

    boolean shouldAccept(EventFilterContext context, long nowMillis) {
        if (!isActive()) {
            return false;
        }
        if (!config.eventClassName().equals(context.eventClassName())) {
            return false;
        }
        if (!config.filter().matches(context)) {
            return false;
        }
        if (SamplingPolicy.requiresNarrowingFilter(config.eventClassName()) && !hasNarrowingFilter()) {
            return false;
        }
        if (!samplingPolicy.accepts(config.eventClassName(), config.filter())) {
            sampledOutEvents++;
            return false;
        }
        if (nowMillis - startedAtMillis >= config.effectiveMaxDurationMillis()) {
            state = TraceSessionState.EXPIRED;
            return false;
        }
        return true;
    }

    Optional<PendingDispatch> beginDispatch(EventFilterContext context, long nowMillis, long nowNanos) {
        if (!shouldAccept(context, nowMillis)) {
            return Optional.empty();
        }

        lastActivityAtMillis = nowMillis;
        return Optional.of(new PendingDispatch(
                sequence.incrementAndGet(),
                nowMillis,
                nowNanos,
                context.cancellable(),
                context.cancelled(),
                context.playerName(),
                context.worldName(),
                context.blockX(),
                context.blockY(),
                context.blockZ()));
    }

    Optional<TraceDispatchRecord> completeDispatch(DispatchCompletion completion, PendingDispatch pending) {
        if (!isActive()) {
            return Optional.empty();
        }

        lastActivityAtMillis = completion.endMillis();

        if (records.size() >= config.effectiveMaxEventCount()) {
            droppedEvents++;
            state = TraceSessionState.FULL;
            return Optional.empty();
        }

        TraceDispatchRecord dispatchRecord =
                TraceDispatchWriter.buildRecord(config, pending, completion, records.size());

        PerformanceBudgetController.BudgetEvaluation evaluation =
                budgetController.recordOverhead(completion.eventLensOverheadNanos());
        if (evaluation.decision() == PerformanceBudgetController.Decision.THROTTLE) {
            throttledCapture = true;
            if (state == TraceSessionState.ACTIVE) {
                state = TraceSessionState.THROTTLED;
            }
        } else if (evaluation.decision() == PerformanceBudgetController.Decision.STOP) {
            stop(TraceSessionState.STOPPED, completion.endMillis());
        }

        synchronized (records) {
            if (records.size() >= config.effectiveMaxEventCount()) {
                droppedEvents++;
                state = TraceSessionState.FULL;
                return Optional.empty();
            }
            records.add(dispatchRecord);
            if (records.size() >= config.effectiveMaxEventCount()) {
                state = TraceSessionState.FULL;
            }
        }
        return Optional.of(dispatchRecord);
    }

    boolean pause(long nowMillis) {
        return setState(isActive(), TraceSessionState.PAUSED, nowMillis);
    }

    boolean resume(long nowMillis) {
        return setState(state == TraceSessionState.PAUSED, TraceSessionState.ACTIVE, nowMillis);
    }

    void stop(TraceSessionState stopState, long nowMillis) {
        setState(isOpen(), stopState, nowMillis);
    }

    private boolean setState(boolean allowed, TraceSessionState next, long nowMillis) {
        if (!allowed) {
            return false;
        }
        state = next;
        lastActivityAtMillis = nowMillis;
        return true;
    }

    private boolean hasNarrowingFilter() {
        return config.filter().pluginName().isPresent()
                || config.filter().playerName().isPresent()
                || config.filter().worldName().isPresent()
                || config.filter().region().isPresent();
    }

    record PendingDispatch(
            long sequence,
            long startedAtMillis,
            long startedAtNanos,
            boolean cancellable,
            boolean cancelledAtStart,
            Optional<String> playerName,
            Optional<String> worldName,
            Optional<Integer> blockX,
            Optional<Integer> blockY,
            Optional<Integer> blockZ) {}
}
