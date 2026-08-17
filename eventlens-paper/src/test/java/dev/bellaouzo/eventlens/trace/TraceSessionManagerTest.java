package dev.bellaouzo.eventlens.trace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.EventFilterContext;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceLimits;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.paper.instrumentation.NoOpInstrumentationAdapter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TraceSessionManagerTest {

    private TraceSessionManager manager;
    private InstrumentationPort instrumentationPort;

    @BeforeEach
    void setUp() {
        manager = new TraceSessionManager();
        instrumentationPort = new NoOpInstrumentationAdapter();
        manager.setInstrumentationPort(instrumentationPort);
    }

    @Test
    void startsWithTracingDisabledAndNoSessions() {
        assertFalse(manager.isTracingEnabled());
        assertEquals(0, manager.getActiveSessionCount());
    }

    @Test
    void startStopAndCaptureDispatch() {
        String sessionId = manager.startSession(defaultConfig("org.example.TestEvent"), "admin", 1_000L);

        EventFilterContext context = new EventFilterContext(
                "org.example.TestEvent",
                true,
                false,
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.of(10),
                Optional.of(64),
                Optional.of(20),
                List.of("PluginA"));

        manager.beginEventDispatch(sessionId, 42L, context, 1_100L, 1_100_000_000L);
        manager.completeEventDispatch(
                sessionId,
                42L,
                new DispatchCompletion(
                        new EventFilterContext(
                                "org.example.TestEvent",
                                true,
                                true,
                                Optional.of("Steve"),
                                Optional.of("world"),
                                Optional.of(10),
                                Optional.of(64),
                                Optional.of(20),
                                List.of("PluginA")),
                        1_150L,
                        1_250_000_000L,
                        50_000L,
                        true,
                        List.of(new TraceListenerSnapshot(1, "PluginA", "Listener", "onEvent", "NORMAL", false)),
                        snapshot("LOWEST"),
                        snapshot("MONITOR"),
                        List.of(snapshot("LOWEST"), snapshot("MONITOR")),
                        List.of(),
                        EnumSet.of(TracePartialReason.AGENT_ABSENT)));

        var capturedDispatch =
                manager.getSessionDetail(sessionId).orElseThrow().records().getFirst();
        assertEquals(150_000_000L, capturedDispatch.durationNanos());
        assertEquals(50_000L, capturedDispatch.eventLensOverheadNanos());
        assertEquals(
                1, manager.getSessionDetail(sessionId).orElseThrow().records().size());
        assertTrue(manager.isTracingEnabled());

        List<String> stopped = manager.stopSessionsForOwner("admin", 2_000L);
        assertEquals(List.of(sessionId), stopped);
        assertFalse(manager.isTracingEnabled());
    }

    @Test
    void enforcesConcurrentSessionLimit() {
        for (int index = 0; index < TraceLimits.MAX_CONCURRENT_SESSIONS; index++) {
            manager.startSession(defaultConfig("org.example.Event" + index), "admin", 1_000L);
        }

        TraceSessionConfig overflowConfig = defaultConfig("org.example.Overflow");
        assertThrows(IllegalStateException.class, () -> startOverflowSession(manager, overflowConfig));
    }

    private static void startOverflowSession(TraceSessionManager manager, TraceSessionConfig config) {
        manager.startSession(config, "admin", 1_000L);
    }

    @Test
    void expiresByDurationAndAbandonment() {
        TraceSessionConfig shortDuration = new TraceSessionConfig(
                "org.example.TestEvent", TraceFilter.unrestricted(), Optional.of(500L), Optional.empty());
        String durationSession = manager.startSession(shortDuration, "admin", 1_000L);

        manager.expireSessions(1_600L);
        assertEquals(
                TraceSessionState.EXPIRED,
                manager.getSessionDetail(durationSession)
                        .orElseThrow()
                        .summary()
                        .state());

        String abandonedSession = manager.startSession(
                new TraceSessionConfig(
                        "org.example.TestEvent",
                        TraceFilter.unrestricted(),
                        Optional.of(TraceLimits.ABANDONED_SESSION_MILLIS * 2L),
                        Optional.empty()),
                "admin",
                10_000L);
        manager.expireSessions(10_000L + TraceLimits.ABANDONED_SESSION_MILLIS + 1);
        assertEquals(
                TraceSessionState.ABANDONED,
                manager.getSessionDetail(abandonedSession)
                        .orElseThrow()
                        .summary()
                        .state());
    }

    @Test
    void pauseStopsCaptureAndResumeContinues() {
        String sessionId = manager.startSession(defaultConfig("org.example.TestEvent"), "admin", 1_000L);
        assertTrue(manager.pauseSession(sessionId, 1_100L).isPresent());
        assertEquals(
                TraceSessionState.PAUSED,
                manager.getSessionDetail(sessionId).orElseThrow().summary().state());
        assertFalse(manager.isTracingEnabled());

        EventFilterContext context = new EventFilterContext(
                "org.example.TestEvent",
                false,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of());
        manager.beginEventDispatch(sessionId, 1L, context, 1_200L, 1_200_000_000L);
        assertTrue(manager.getSessionDetail(sessionId).orElseThrow().records().isEmpty());

        assertTrue(manager.resumeSession(sessionId, 1_300L).isPresent());
        assertTrue(manager.isTracingEnabled());
        manager.beginEventDispatch(sessionId, 2L, context, 1_400L, 1_400_000_000L);
        manager.completeEventDispatch(
                sessionId,
                2L,
                new DispatchCompletion(
                        context,
                        1_450L,
                        1_450_000_000L,
                        10_000L,
                        false,
                        List.of(),
                        snapshot("LOWEST"),
                        snapshot("MONITOR"),
                        List.of(),
                        List.of(),
                        EnumSet.noneOf(TracePartialReason.class)));
        assertEquals(
                1, manager.getSessionDetail(sessionId).orElseThrow().records().size());
        assertEquals(List.of(sessionId), manager.stopSessionsForOwner("admin", 2_000L));
    }

    @Test
    void restartReusesSameSessionId() {
        String sourceId = manager.startSession(defaultConfig("org.example.TestEvent"), "admin", 1_000L);
        assertEquals(List.of(sourceId), manager.stopSessionsForOwner("admin", 2_000L));

        TraceRestartResult result = manager.restartSession(sourceId, 3_000L);
        assertTrue(result instanceof TraceRestartResult.Success);
        TraceRestartResult.Success success = (TraceRestartResult.Success) result;
        assertEquals(sourceId, success.sessionId());
        assertEquals(sourceId, success.sourceSessionId());
        assertEquals(1, success.restartCount());
        assertEquals("org.example.TestEvent", success.eventClassName());
        assertTrue(manager.isTracingEnabled());
        assertEquals(
                TraceSessionState.ACTIVE,
                manager.getSessionDetail(sourceId).orElseThrow().summary().state());
        assertEquals(
                1, manager.getSessionDetail(sourceId).orElseThrow().summary().restartCount());
        assertTrue(manager.getSessionDetail(sourceId).orElseThrow().summary().restarted());
        assertEquals(
                "RESTARTED",
                manager.getSessionDetail(sourceId).orElseThrow().summary().restartBadge());
        assertEquals(2, manager.listGenerations(sourceId).size());
        assertEquals(0, manager.listGenerations(sourceId).getFirst().generation());
        assertTrue(manager.listGenerations(sourceId).getLast().current());
        assertTrue(manager.getSessionDetail(sourceId, Optional.of(0)).isPresent());
        assertTrue(manager.restartSession(sourceId, 4_000L) instanceof TraceRestartResult.StillOpen);
    }

    @Test
    void closeAllResetsState() {
        manager.startSession(defaultConfig("org.example.TestEvent"), "admin", 1_000L);

        manager.closeAll();

        assertFalse(manager.isTracingEnabled());
        assertEquals(0, manager.getActiveSessionCount());
    }

    private static TraceSessionConfig defaultConfig(String eventClassName) {
        return new TraceSessionConfig(eventClassName, TraceFilter.unrestricted(), Optional.empty(), Optional.empty());
    }

    private static EventSnapshot snapshot(String checkpoint) {
        return new EventSnapshot("org.example.TestEvent", checkpoint, 1_000L, List.of());
    }
}
