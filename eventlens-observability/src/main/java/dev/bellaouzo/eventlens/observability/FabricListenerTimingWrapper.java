package dev.bellaouzo.eventlens.observability;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

public final class FabricListenerTimingWrapper {

    private static final int MAX_STACK_FRAMES = 12;
    private static final Object ANONYMOUS_DISPATCH_KEY = new Object();

    private FabricListenerTimingWrapper() {}

    public static Object wrap(Object listener, Class<?> listenerType, String phaseId) {
        if (listener == null || listenerType == null || !listenerType.isInterface()) {
            return listener;
        }
        if (listener instanceof EventLensFabricTimingProxy) {
            return listener;
        }
        ClassLoader loader = listener.getClass().getClassLoader();
        if (loader == null) {
            loader = FabricListenerTimingWrapper.class.getClassLoader();
        }
        return Proxy.newProxyInstance(
                loader,
                new Class<?>[] {listenerType, EventLensFabricTimingProxy.class},
                new TimingHandler(listener, phaseId));
    }

    public static Object unwrap(Object listener) {
        if (listener instanceof EventLensFabricTimingProxy proxy) {
            return proxy.eventLensUnwrap();
        }
        return listener;
    }

    private static final class TimingHandler implements InvocationHandler, EventLensFabricTimingProxy {

        private final Object delegate;
        private final String phaseId;

        private TimingHandler(Object delegate, String phaseId) {
            this.delegate = delegate;
            this.phaseId = phaseId == null ? "default" : phaseId;
        }

        @Override
        public Object eventLensUnwrap() {
            return delegate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == EventLensFabricTimingProxy.class) {
                return method.invoke(this, args);
            }
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(delegate, args);
            }
            if (!ObservationGate.isEnabled()) {
                return invokeDelegate(method, args);
            }
            Object dispatchKeySource = firstNonNull(args);
            long eventKey = dispatchKeySource != null
                    ? Integer.toUnsignedLong(System.identityHashCode(dispatchKeySource))
                    : Integer.toUnsignedLong(System.identityHashCode(ANONYMOUS_DISPATCH_KEY));
            DispatchObservationRegistry.beginDispatch(eventKey);
            ListenerMetadata metadata = metadata(delegate, method.getName(), phaseId);
            CompactEventSnapshot beforeSnapshot = captureSnapshot(dispatchKeySource, metadata, "before");
            if (beforeSnapshot != null) {
                DispatchObservationRegistry.storeListenerBeforeSnapshot(beforeSnapshot);
            }
            long startNanos = System.nanoTime();
            Throwable thrown = null;
            try {
                return invokeDelegate(method, args);
            } catch (Throwable ex) {
                thrown = ex;
                throw ex;
            } finally {
                long durationNanos = Math.max(0L, System.nanoTime() - startNanos);
                String stackTrace = null;
                if (AgentRuntime.captureStacks() && durationNanos >= AgentRuntime.slowThresholdNanos()) {
                    stackTrace = captureStackTrace();
                }
                Optional<CompactEventSnapshot> before = DispatchObservationRegistry.takeListenerBeforeSnapshot();
                CompactEventSnapshot afterSnapshot = captureSnapshot(dispatchKeySource, metadata, "after");
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
                        before.orElse(null),
                        afterSnapshot);
            }
        }

        private Object invokeDelegate(Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException ex) {
                throw ex.getCause() == null ? ex : ex.getCause();
            }
        }
    }

    private static ListenerMetadata metadata(Object listener, String methodName, String phaseId) {
        Object target = unwrap(listener);
        String owner = AgentRuntime.resolveOwnerId(target);
        return new ListenerMetadata(owner, target.getClass().getName(), methodName, phaseId);
    }

    private static CompactEventSnapshot captureSnapshot(Object dispatchKeySource, ListenerMetadata metadata, String phase) {
        ListenerSnapshotBridge bridge = AgentRuntime.listenerSnapshotBridge();
        if (bridge == null || dispatchKeySource == null) {
            return null;
        }
        String checkpoint = "L" + phase + ":" + metadata.pluginName() + ":" + metadata.priority();
        return bridge.capture(dispatchKeySource, checkpoint);
    }

    private static Object firstNonNull(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg != null) {
                return arg;
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

    public interface EventLensFabricTimingProxy {
        Object eventLensUnwrap();
    }

    private record ListenerMetadata(
            String pluginName, String listenerClassName, String methodName, String priority) {}
}
