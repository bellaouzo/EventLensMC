package dev.bellaouzo.eventlens.observability;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import net.bytebuddy.asm.Advice;

public final class EventListenerTimingAdvice {

    private static final int MAX_STACK_FRAMES = 12;

    private EventListenerTimingAdvice() {}

    @Advice.OnMethodEnter
    public static long onEnter(@Advice.This Object listener, @Advice.Argument(0) Object event) {
        if (!ObservationGate.isEnabled()) {
            return -1L;
        }
        long eventKey = Integer.toUnsignedLong(System.identityHashCode(event));
        DispatchObservationRegistry.beginDispatch(eventKey);
        ListenerMetadata metadata = extractMetadata(listener);
        CompactEventSnapshot beforeSnapshot = captureSnapshot(event, metadata, "before");
        if (beforeSnapshot != null) {
            DispatchObservationRegistry.storeListenerBeforeSnapshot(beforeSnapshot);
        }
        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(
            @Advice.This Object listener,
            @Advice.Argument(0) Object event,
            @Advice.Enter long startNanos,
            @Advice.Thrown Throwable thrown) {
        if (startNanos < 0L) {
            return;
        }
        long durationNanos = Math.max(0L, System.nanoTime() - startNanos);
        long eventKey = Integer.toUnsignedLong(System.identityHashCode(event));
        ListenerMetadata metadata = extractMetadata(listener);
        String stackTrace = null;
        if (AgentRuntime.captureStacks() && durationNanos >= AgentRuntime.slowThresholdNanos()) {
            stackTrace = captureStackTrace();
        }
        Optional<CompactEventSnapshot> beforeSnapshot = DispatchObservationRegistry.takeListenerBeforeSnapshot();
        CompactEventSnapshot afterSnapshot = captureSnapshot(event, metadata, "after");
        DispatchObservationRegistry.recordListener(
                eventKey,
                metadata.pluginName(),
                metadata.listenerClassName(),
                metadata.methodName(),
                metadata.priority(),
                durationNanos,
                true,
                stackTrace,
                thrown,
                beforeSnapshot.orElse(null),
                afterSnapshot);
    }

    private static CompactEventSnapshot captureSnapshot(Object event, ListenerMetadata metadata, String phase) {
        ListenerSnapshotBridge bridge = AgentRuntime.listenerSnapshotBridge();
        if (bridge == null) {
            return null;
        }
        String checkpoint = "L" + phase + ":" + metadata.pluginName() + ":" + metadata.priority();
        return bridge.capture(event, checkpoint);
    }

    private static ListenerMetadata extractMetadata(Object listener) {
        String owner = AgentRuntime.resolveOwnerId(listener);
        String className = ownerClassName(listener);
        String methodName = methodName(listener);
        String priority = priorityName(listener);
        return new ListenerMetadata(owner, className, methodName, priority);
    }

    private static String ownerClassName(Object listener) {
        Object consumer = fieldValue(listener, "consumer");
        if (consumer != null) {
            return consumer.getClass().getName();
        }
        return listener.getClass().getName();
    }

    private static String methodName(Object listener) {
        String readable = String.valueOf(listener);
        int paren = readable.lastIndexOf('(');
        if (paren > 0) {
            int space = readable.lastIndexOf(' ', paren);
            if (space >= 0 && space + 1 < paren) {
                return readable.substring(space + 1, paren);
            }
        }
        return "<lambda>";
    }

    private static String priorityName(Object listener) {
        try {
            Method method = listener.getClass().getMethod("getPriority");
            Object priority = method.invoke(listener);
            return priority == null ? "UNKNOWN" : priority.toString();
        } catch (ReflectiveOperationException ignored) {
            return "UNKNOWN";
        }
    }

    private static Object fieldValue(Object target, String fieldName) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    private static String captureStackTrace() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder builder = new StringBuilder();
        int frames = 0;
        for (StackTraceElement element : stack) {
            if (element.getClassName().contains("bytebuddy")
                    || element.getClassName().contains("eventlens.observability")) {
                continue;
            }
            if (frames > 0) {
                builder.append('\n');
            }
            builder.append("  at ").append(element);
            frames++;
            if (frames >= MAX_STACK_FRAMES) {
                break;
            }
        }
        return builder.toString();
    }

    private record ListenerMetadata(
            String pluginName, String listenerClassName, String methodName, String priority) {}
}
