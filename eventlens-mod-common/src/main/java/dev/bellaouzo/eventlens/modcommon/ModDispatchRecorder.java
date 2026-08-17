package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.trace.EventFilterContext;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.trace.DispatchCompletion;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class ModDispatchRecorder {

    private final TraceSessionManager sessionManager;
    private final ModListenerTimingSource timingSource;
    private final ModDispatchFlushScheduler flushScheduler;
    private final AtomicLong dispatchSequence = new AtomicLong();
    private final Map<Long, Pending> pendingByDispatch = new ConcurrentHashMap<>();
    private final Map<String, long[]> lastMove = new ConcurrentHashMap<>();

    public ModDispatchRecorder(TraceSessionManager sessionManager) {
        this(sessionManager, null, Runnable::run);
    }

    public ModDispatchRecorder(
            TraceSessionManager sessionManager,
            ModListenerTimingSource timingSource,
            ModDispatchFlushScheduler flushScheduler) {
        this.sessionManager = sessionManager;
        this.timingSource = timingSource;
        this.flushScheduler = flushScheduler == null ? Runnable::run : flushScheduler;
    }

    public boolean isTracing() {
        return sessionManager.isTracingEnabled();
    }

    public void recordImmediate(
            String eventClassName, List<SnapshotField> fields, Optional<String> playerName, Optional<String> worldName) {
        recordImmediate(eventClassName, fields, playerName, worldName, null);
    }

    public void recordImmediate(
            String eventClassName,
            List<SnapshotField> fields,
            Optional<String> playerName,
            Optional<String> worldName,
            Object platformEvent) {
        long dispatchKey = beginPaired(eventClassName, playerName, worldName, platformEvent);
        endPaired(dispatchKey, eventClassName, fields, playerName, worldName, platformEvent);
    }

    public long beginPaired(String eventClassName, Optional<String> playerName, Optional<String> worldName) {
        return beginPaired(eventClassName, playerName, worldName, null);
    }

    public long beginPaired(
            String eventClassName, Optional<String> playerName, Optional<String> worldName, Object platformEvent) {
        List<String> sessionIds = sessionManager.getActiveSessionIdsForEvent(eventClassName);
        if (sessionIds.isEmpty()) {
            return -1L;
        }
        long dispatchKey = dispatchSequence.incrementAndGet();
        long startNanos = System.nanoTime();
        long nowMillis = System.currentTimeMillis();
        Pending pending = new Pending(eventClassName, playerName, worldName);
        pending.addObservationKey(platformEvent);
        pendingByDispatch.put(dispatchKey, pending);
        EventFilterContext context = ModDispatchCompletions.context(eventClassName, playerName, worldName);
        for (String sessionId : sessionIds) {
            sessionManager.beginEventDispatch(sessionId, dispatchKey, context, nowMillis, startNanos);
        }
        return dispatchKey;
    }

    public void endPaired(
            long dispatchKey,
            String eventClassName,
            List<SnapshotField> fields,
            Optional<String> playerName,
            Optional<String> worldName) {
        endPaired(dispatchKey, eventClassName, fields, playerName, worldName, null);
    }

    public void endPaired(
            long dispatchKey,
            String eventClassName,
            List<SnapshotField> fields,
            Optional<String> playerName,
            Optional<String> worldName,
            Object platformEvent) {
        if (dispatchKey < 0L) {
            return;
        }
        Pending pending = pendingByDispatch.get(dispatchKey);
        if (pending == null) {
            return;
        }
        pending.eventClassName = eventClassName;
        pending.fields = fields;
        pending.playerName = playerName;
        pending.worldName = worldName;
        pending.addObservationKey(platformEvent);
        if (timingSource == null) {
            flush(dispatchKey);
            return;
        }
        flushScheduler.afterCurrentDispatch(() -> flush(dispatchKey));
    }

    public void recordMoveIfChanged(
            String playerName, String worldName, double x, double y, double z, float yaw, float pitch) {
        if (!sessionManager.isTracingEnabled()) {
            return;
        }
        long packedX = Double.doubleToLongBits(x);
        long packedY = Double.doubleToLongBits(y);
        long packedZ = Double.doubleToLongBits(z);
        long[] previous = lastMove.put(playerName, new long[] {packedX, packedY, packedZ});
        if (previous == null || (previous[0] == packedX && previous[1] == packedY && previous[2] == packedZ)) {
            return;
        }
        recordImmediate(
                SupportedModEventTypes.CLIENT_PLAYER_MOVE_EVENT,
                List.of(
                        ModSnapshotFields.number("x", x),
                        ModSnapshotFields.number("y", y),
                        ModSnapshotFields.number("z", z),
                        ModSnapshotFields.number("yaw", yaw),
                        ModSnapshotFields.number("pitch", pitch)),
                Optional.of(playerName),
                Optional.of(worldName));
    }

    void flush(long dispatchKey) {
        Pending pending = pendingByDispatch.remove(dispatchKey);
        if (pending == null) {
            return;
        }
        List<String> sessionIds = sessionManager.getActiveSessionIdsForEvent(pending.eventClassName);
        if (sessionIds.isEmpty()) {
            return;
        }
        long endNanos = System.nanoTime();
        long endMillis = System.currentTimeMillis();
        EventFilterContext context =
                ModDispatchCompletions.context(pending.eventClassName, pending.playerName, pending.worldName);
        EventSnapshot snapshot = ModDispatchCompletions.snapshot(
                pending.eventClassName, endMillis, endNanos, pending.fields);
        List<ListenerTimingRecord> timings = consumeTimings(pending);
        boolean agentPresent = timingSource != null;
        boolean snapshotsEnabled = sessionManager.getInstrumentationPort() != null
                && sessionManager.getInstrumentationPort().listenerSnapshotsEnabled();
        DispatchCompletion completion = ModDispatchCompletions.completion(
                context, endMillis, endNanos, snapshot, timings, agentPresent, snapshotsEnabled);
        for (String sessionId : sessionIds) {
            sessionManager.completeEventDispatch(sessionId, dispatchKey, completion);
        }
    }

    private List<ListenerTimingRecord> consumeTimings(Pending pending) {
        if (timingSource == null) {
            return List.of();
        }
        long threshold = sessionManager.minSlowThresholdForEvent(pending.eventClassName);
        List<ListenerTimingRecord> timings = new ArrayList<>();
        for (long observationKey : pending.observationKeys) {
            timings.addAll(timingSource.consume(observationKey, threshold));
        }
        return ModDispatchCompletions.renumber(timings);
    }

    private static final class Pending {
        private String eventClassName;
        private List<SnapshotField> fields = List.of();
        private Optional<String> playerName;
        private Optional<String> worldName;
        private final List<Long> observationKeys = new ArrayList<>();

        private Pending(String eventClassName, Optional<String> playerName, Optional<String> worldName) {
            this.eventClassName = eventClassName;
            this.playerName = playerName;
            this.worldName = worldName;
        }

        private void addObservationKey(Object platformEvent) {
            if (platformEvent == null) {
                return;
            }
            long key = Integer.toUnsignedLong(System.identityHashCode(platformEvent));
            if (!observationKeys.contains(key)) {
                observationKeys.add(key);
            }
        }
    }
}
