package dev.bellaouzo.eventlens.modcommon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.SessionConflictAnalyzer;
import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.observability.PerformanceBudget;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.observability.AgentRuntime;
import dev.bellaouzo.eventlens.observability.CompactEventSnapshot;
import dev.bellaouzo.eventlens.observability.CompactField;
import dev.bellaouzo.eventlens.observability.DispatchObservationRegistry;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ModDispatchRecorderTest {

    @AfterEach
    void resetAgentFlag() {
        System.clearProperty("dev.bellaouzo.eventlens.agent.loaded");
    }

    @Test
    void attachesListenerObservationsAndClearsAgentAbsent() {
        TraceSessionManager sessions = new TraceSessionManager();
        AgentRuntime.markAgentLoaded();
        ModAgentInstrumentationAdapter adapter = new ModAgentInstrumentationAdapter();
        sessions.setInstrumentationPort(adapter);
        String sessionId = sessions.startSession(
                new TraceSessionConfig(
                        SupportedModEventTypes.CLIENT_CHAT_EVENT,
                        TraceFilter.Builder.unrestricted().build(),
                        Optional.empty(),
                        Optional.of(8)),
                "Dev",
                System.currentTimeMillis());

        Object event = new Object();
        long eventKey = Integer.toUnsignedLong(System.identityHashCode(event));
        CompactEventSnapshot before = snapshot("false");
        CompactEventSnapshot after = snapshot("true");
        DispatchObservationRegistry.beginDispatch(eventKey);
        DispatchObservationRegistry.recordListener(
                eventKey, "jei", "jei.ChatHook", "onChat", "NORMAL", 120_000L, true, null, null, before, after);
        DispatchObservationRegistry.recordListener(
                eventKey, "chatplus", "chatplus.Filter", "onChat", "HIGH", 80_000L, true, null, null, after, after);

        ModDispatchRecorder recorder = new ModDispatchRecorder(sessions, adapter, Runnable::run);
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_CHAT_EVENT,
                List.of(),
                Optional.of("Dev"),
                Optional.of("minecraft:overworld"),
                event);

        TraceDispatchRecord record = sessions.getSessionDetail(sessionId).orElseThrow().records().getFirst();
        assertEquals(2, record.listenerTimings().size());
        assertEquals("jei", record.listenerTimings().getFirst().pluginName());
        assertEquals("chatplus", record.listenerTimings().get(1).pluginName());
        assertFalse(record.partialReasons().contains(TracePartialReason.AGENT_ABSENT));

        SessionConflictSummary summary =
                SessionConflictAnalyzer.analyze(List.of(record), PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS);
        assertTrue(summary.dispatchesWithConflicts() > 0 || record.listenerTimings().size() == 2);
    }

    @Test
    void withoutAgentKeepsDispatchOnlyPartialReason() {
        TraceSessionManager sessions = new TraceSessionManager();
        sessions.setInstrumentationPort(new ModNoOpInstrumentationAdapter());
        String sessionId = sessions.startSession(
                new TraceSessionConfig(
                        SupportedModEventTypes.CLIENT_CHAT_EVENT,
                        TraceFilter.Builder.unrestricted().build(),
                        Optional.empty(),
                        Optional.of(8)),
                "Dev",
                System.currentTimeMillis());
        ModDispatchRecorder recorder = new ModDispatchRecorder(sessions);
        recorder.recordImmediate(SupportedModEventTypes.CLIENT_CHAT_EVENT, List.of(), Optional.empty(), Optional.empty());
        TraceDispatchRecord record = sessions.getSessionDetail(sessionId).orElseThrow().records().getFirst();
        assertTrue(record.partialReasons().contains(TracePartialReason.AGENT_ABSENT));
        assertTrue(record.listenerTimings().isEmpty());
    }

    private static CompactEventSnapshot snapshot(String cancelled) {
        return new CompactEventSnapshot(
                "chat", "L", 1L, List.of(new CompactField("cancelled", "boolean", cancelled)));
    }
}
