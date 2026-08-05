package dev.bellaouzo.eventlens.domain.conflict;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListenerChainAnalyzerTest {

    @Test
    void detectsDuplicateRegistrations() {
        List<TraceListenerSnapshot> chain = List.of(
                listener("PluginA", "com.example.A", "onEvent", "NORMAL"),
                listener("PluginA", "com.example.A", "onEvent", "NORMAL"));

        List<DispatchConflict> conflicts = ListenerChainAnalyzer.detectChainIssues(chain);

        assertEquals(1, conflicts.size());
        assertEquals(ConflictKind.DUPLICATE_REGISTRATION, conflicts.getFirst().kind());
    }

    @Test
    void detectsEquivalentListenersFromSamePluginAtPriority() {
        List<ListenerRegistration> registrations = List.of(
                registration("PluginA", "com.example.A", "onA", "HIGH"),
                registration("PluginA", "com.example.B", "onB", "HIGH"));

        List<DispatchConflict> conflicts = ListenerChainAnalyzer.detectInventoryIssues(registrations);

        assertEquals(1, conflicts.size());
        assertEquals(ConflictKind.EQUIVALENT_LISTENER, conflicts.getFirst().kind());
        assertTrue(conflicts.getFirst().message().contains("PluginA"));
    }

    private static TraceListenerSnapshot listener(String plugin, String listenerClass, String method, String priority) {
        return new TraceListenerSnapshot(1, plugin, listenerClass, method, priority, false);
    }

    private static ListenerRegistration registration(
            String plugin, String listenerClass, String method, String priority) {
        return new ListenerRegistration(1, plugin, listenerClass, method, priority, false);
    }
}
