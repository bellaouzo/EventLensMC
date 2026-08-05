package dev.bellaouzo.eventlens.observability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class DispatchObservationRegistry {

    private static final ThreadLocal<CompactEventSnapshot> LISTENER_BEFORE_SNAPSHOT = new ThreadLocal<>();
    private static final ThreadLocal<Long> ACTIVE_DISPATCH_KEY = new ThreadLocal<>();
    private static final Map<Long, DispatchBuffer> BUFFERS = new ConcurrentHashMap<>();

    private DispatchObservationRegistry() {}

    public static void beginDispatch(long eventKey) {
        ACTIVE_DISPATCH_KEY.set(eventKey);
        BUFFERS.computeIfAbsent(eventKey, ignored -> new DispatchBuffer());
    }

    public static void recordListener(
            long eventKey,
            String pluginName,
            String listenerClassName,
            String methodName,
            String priority,
            long durationNanos,
            boolean mainThread,
            String stackTrace,
            Throwable thrown,
            CompactEventSnapshot snapshotBefore,
            CompactEventSnapshot snapshotAfter) {
        DispatchBuffer buffer = BUFFERS.get(eventKey);
        if (buffer == null) {
            return;
        }
        buffer.record(
                pluginName,
                listenerClassName,
                methodName,
                priority,
                durationNanos,
                mainThread,
                stackTrace,
                thrown,
                snapshotBefore,
                snapshotAfter);
    }

    public static List<ListenerObservation> finishDispatch(long eventKey) {
        ACTIVE_DISPATCH_KEY.remove();
        LISTENER_BEFORE_SNAPSHOT.remove();
        DispatchBuffer buffer = BUFFERS.remove(eventKey);
        if (buffer == null) {
            return List.of();
        }
        return buffer.finish();
    }

    public static void clear(long eventKey) {
        ACTIVE_DISPATCH_KEY.remove();
        LISTENER_BEFORE_SNAPSHOT.remove();
        BUFFERS.remove(eventKey);
    }

    public static Long activeDispatchKey() {
        return ACTIVE_DISPATCH_KEY.get();
    }

    static void storeListenerBeforeSnapshot(CompactEventSnapshot snapshot) {
        LISTENER_BEFORE_SNAPSHOT.set(snapshot);
    }

    static Optional<CompactEventSnapshot> takeListenerBeforeSnapshot() {
        CompactEventSnapshot snapshot = LISTENER_BEFORE_SNAPSHOT.get();
        LISTENER_BEFORE_SNAPSHOT.remove();
        return Optional.ofNullable(snapshot);
    }

    private static final class DispatchBuffer {

        private final AtomicInteger order = new AtomicInteger();
        private final List<ListenerObservation> observations = Collections.synchronizedList(new ArrayList<>());

        private void record(
                String pluginName,
                String listenerClassName,
                String methodName,
                String priority,
                long durationNanos,
                boolean mainThread,
                String stackTrace,
                Throwable thrown,
                CompactEventSnapshot snapshotBefore,
                CompactEventSnapshot snapshotAfter) {
            int invocationOrder = order.incrementAndGet();
            boolean threwException = thrown != null;
            java.util.Optional<String> exceptionType = threwException
                    ? java.util.Optional.of(thrown.getClass().getName())
                    : java.util.Optional.empty();
            observations.add(new ListenerObservation(
                    invocationOrder,
                    pluginName,
                    listenerClassName,
                    methodName,
                    priority,
                    durationNanos,
                    mainThread,
                    stackTrace == null ? java.util.Optional.empty() : java.util.Optional.of(stackTrace),
                    threwException,
                    exceptionType,
                    snapshotBefore == null ? java.util.Optional.empty() : java.util.Optional.of(snapshotBefore),
                    snapshotAfter == null ? java.util.Optional.empty() : java.util.Optional.of(snapshotAfter)));
        }

        private List<ListenerObservation> finish() {
            synchronized (observations) {
                return List.copyOf(observations);
            }
        }
    }
}
