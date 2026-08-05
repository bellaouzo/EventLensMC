package dev.bellaouzo.eventlens.observability;

import java.lang.reflect.Method;
import java.util.Optional;
import net.bytebuddy.asm.Advice;

public final class RegisteredListenerTimingAdvice {

    private static final int MAX_STACK_FRAMES = 12;

    private RegisteredListenerTimingAdvice() {}

    @Advice.OnMethodEnter
    public static long onEnter(@Advice.This Object registeredListener, @Advice.Argument(0) Object event) {
        if (!ObservationGate.isEnabled()) {
            return -1L;
        }
        long eventKey = Integer.toUnsignedLong(System.identityHashCode(event));
        DispatchObservationRegistry.beginDispatch(eventKey);

        ListenerMetadata metadata = extractMetadata(registeredListener);
        CompactEventSnapshot beforeSnapshot = captureSnapshot(event, metadata, "before");
        if (beforeSnapshot != null) {
            DispatchObservationRegistry.storeListenerBeforeSnapshot(beforeSnapshot);
        }

        return System.nanoTime();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(
            @Advice.This Object registeredListener,
            @Advice.Argument(0) Object event,
            @Advice.Enter long startNanos,
            @Advice.Thrown Throwable thrown) {
        if (startNanos < 0L) {
            return;
        }

        long durationNanos = Math.max(0L, System.nanoTime() - startNanos);
        long eventKey = Integer.toUnsignedLong(System.identityHashCode(event));
        ListenerMetadata metadata = extractMetadata(registeredListener);
        boolean mainThread = isMainThread(event);
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
                mainThread,
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

    private static ListenerMetadata extractMetadata(Object registeredListener) {
        try {
            Object plugin = invoke(registeredListener, "getPlugin");
            String pluginName = plugin == null ? "unknown" : String.valueOf(invoke(plugin, "getName"));
            Object priority = invoke(registeredListener, "getPriority");
            String priorityName = priority == null ? "UNKNOWN" : priority.toString();
            Object listener = invoke(registeredListener, "getListener");
            String listenerClassName =
                    listener == null ? "unknown" : listener.getClass().getName();
            return new ListenerMetadata(pluginName, listenerClassName, "<lambda>", priorityName);
        } catch (ReflectiveOperationException ex) {
            return new ListenerMetadata("unknown", registeredListener.getClass().getName(), "<unknown>", "UNKNOWN");
        }
    }

    private static boolean isMainThread(Object event) {
        try {
            Method asyncMethod = event.getClass().getMethod("isAsynchronous");
            Object result = asyncMethod.invoke(event);
            if (result instanceof Boolean asynchronous && asynchronous) {
                return false;
            }
        } catch (ReflectiveOperationException ignored) {
            // default to main thread for sync events
        }
        return Thread.currentThread().getName().equals("Server thread");
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

    private static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private record ListenerMetadata(
            String pluginName, String listenerClassName, String methodName, String priority) {}
}
