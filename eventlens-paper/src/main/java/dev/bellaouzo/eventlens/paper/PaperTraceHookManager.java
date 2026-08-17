package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.paper.instrumentation.AgentInstrumentationAdapter;
import dev.bellaouzo.eventlens.paper.snapshot.PaperEventSnapshotCapture;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class PaperTraceHookManager implements TraceHookPort {

    private static final EventPriority[] CHECKPOINT_PRIORITIES = {
        EventPriority.LOWEST,
        EventPriority.LOW,
        EventPriority.NORMAL,
        EventPriority.HIGH,
        EventPriority.HIGHEST,
        EventPriority.MONITOR
    };

    private final Plugin plugin;
    private final TraceSessionManager traceSessionManager;
    private final ListenerRegistryPort listenerRegistryPort;
    private final InstrumentationPort instrumentationPort;
    private final AgentInstrumentationAdapter agentAdapter;
    private final EventClassIndex eventClassIndex;
    private final PaperEventSnapshotCapture snapshotCapture = new PaperEventSnapshotCapture();
    private final Listener listenerToken = new Listener() {};
    private final Set<String> hookedEventClassNames = new HashSet<>();
    private final ConcurrentHashMap<Long, PaperDispatchCapture.PendingDispatchCapture> pendingCaptures =
            new ConcurrentHashMap<>();

    public PaperTraceHookManager(
            Plugin plugin,
            TraceSessionManager traceSessionManager,
            ListenerRegistryPort listenerRegistryPort,
            InstrumentationPort instrumentationPort,
            ClassLoader classLoader) {
        this.plugin = plugin;
        this.traceSessionManager = traceSessionManager;
        this.listenerRegistryPort = listenerRegistryPort;
        this.instrumentationPort = instrumentationPort;
        this.agentAdapter = instrumentationPort instanceof AgentInstrumentationAdapter adapter ? adapter : null;
        this.eventClassIndex = new EventClassIndex(classLoader);
    }

    @Override
    public void registerHooksForEvent(String eventClassName) {
        if (hookedEventClassNames.contains(eventClassName)) {
            return;
        }

        Class<? extends Event> eventClass = eventClassIndex.resolveClassName(eventClassName);
        for (EventPriority priority : CHECKPOINT_PRIORITIES) {
            plugin.getServer()
                    .getPluginManager()
                    .registerEvent(
                            eventClass,
                            listenerToken,
                            priority,
                            (listener, event) -> observeCheckpoint(event, priority),
                            plugin,
                            false);
        }
        hookedEventClassNames.add(eventClassName);
    }

    @Override
    public void syncWithActiveSessions(TraceSessionManager manager) {
        List<String> activeEvents = manager.getActiveEventClassNames();
        Set<String> activeSet = new HashSet<>(activeEvents);

        for (String hookedEvent : new HashSet<>(hookedEventClassNames)) {
            if (!activeSet.contains(hookedEvent)) {
                unregisterHooksForEvent(hookedEvent);
            }
        }

        for (String activeEvent : activeEvents) {
            registerHooksForEvent(activeEvent);
        }
    }

    private void unregisterHooksForEvent(String eventClassName) {
        if (!hookedEventClassNames.contains(eventClassName)) {
            return;
        }

        Class<? extends Event> eventClass = eventClassIndex.resolveClassName(eventClassName);
        getHandlerList(eventClass).unregister(plugin);
        hookedEventClassNames.remove(eventClassName);
    }

    private void observeCheckpoint(Event event, EventPriority priority) {
        EventLensCheckpointMarker.beginCheckpoint();
        try {
            long dispatchKey = Integer.toUnsignedLong(System.identityHashCode(event));
            boolean throttled = traceSessionManager.isThrottledCaptureForEvent(
                    event.getClass().getName());
            EventSnapshot snapshot = throttled && priority != EventPriority.LOWEST && priority != EventPriority.MONITOR
                    ? null
                    : snapshotCapture.capture(event, priority.name());

            if (priority == EventPriority.LOWEST) {
                PaperDispatchCapture.PendingDispatchCapture capture =
                        PaperDispatchCapture.createCapture(event, snapshot, listenerRegistryPort);
                pendingCaptures.put(dispatchKey, capture);
                PaperDispatchCapture.beginSessions(event, dispatchKey, capture, traceSessionManager);
                return;
            }

            if (priority == EventPriority.MONITOR) {
                PaperDispatchCapture.PendingDispatchCapture capture = pendingCaptures.remove(dispatchKey);
                if (capture != null) {
                    PaperDispatchCapture.finishDispatchCapture(
                            event,
                            dispatchKey,
                            snapshot,
                            capture,
                            traceSessionManager,
                            instrumentationPort,
                            agentAdapter);
                }
                return;
            }

            if (snapshot != null) {
                PaperDispatchCapture.PendingDispatchCapture capture = pendingCaptures.get(dispatchKey);
                if (capture != null) {
                    capture.checkpoints.add(snapshot);
                }
            }
        } finally {
            long checkpointOverhead = EventLensCheckpointMarker.endCheckpoint();
            PaperDispatchCapture.PendingDispatchCapture capture =
                    pendingCaptures.get(Integer.toUnsignedLong(System.identityHashCode(event)));
            if (capture != null) {
                capture.eventLensOverheadNanos += checkpointOverhead;
            }
        }
    }

    private static HandlerList getHandlerList(Class<? extends Event> eventClass) {
        try {
            return (HandlerList) eventClass.getMethod("getHandlerList").invoke(null);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Event class does not expose getHandlerList(): " + eventClass.getName(), ex);
        }
    }
}
