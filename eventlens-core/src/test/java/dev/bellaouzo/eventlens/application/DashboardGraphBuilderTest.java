package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraph;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraphNode;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DashboardGraphBuilderTest {

    @Test
    void buildsEventRelationshipGraphFromListenerInventory() {
        ListenerRegistryPort registry = stubRegistry();
        DashboardGraph graph = DashboardGraphBuilder.buildEventRelationshipGraph(registry);

        assertEquals(3, graph.nodes().size());
        assertEquals(2, graph.edges().size());
        assertTrue(graph.nodes().stream().anyMatch(node -> node.kind() == DashboardGraphNode.Kind.EVENT));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.kind() == DashboardGraphNode.Kind.PLUGIN));
    }

    @Test
    void buildsPluginInteractionGraphFromSharedEventsAndTrace() {
        TraceSessionManager manager = new TraceSessionManager();
        String sessionId = manager.startSession(
                new TraceSessionConfig(
                        "org.bukkit.event.block.BlockBreakEvent",
                        TraceFilter.unrestricted(),
                        Optional.empty(),
                        Optional.empty()),
                "Admin",
                1_000L);
        manager.stopSession(sessionId, 2_000L);

        DashboardGraph graph = DashboardGraphBuilder.buildPluginInteractionGraph(
                stubRegistry(), manager, java.util.Optional.of(sessionId));

        assertTrue(graph.nodes().size() >= 2);
    }

    private static ListenerRegistryPort stubRegistry() {
        return new ListenerRegistryPort() {
            @Override
            public EventSearchResult searchEvents(String query) {
                return EventSearchResult.found("org.bukkit.event.block.BlockBreakEvent");
            }

            @Override
            public List<ListenerRegistration> getListeners(String eventClassName) {
                return List.of(
                        new ListenerRegistration(
                                1, "EventLensTestTarget", "dev.test.Listener", "onBreak", "NORMAL", false),
                        new ListenerRegistration(2, "OtherPlugin", "dev.other.Listener", "onBreak", "HIGH", false));
            }

            @Override
            public List<String> listKnownEventSimpleNames() {
                return List.of("BlockBreakEvent");
            }

            @Override
            public List<String> listKnownEventClassNames() {
                return List.of("org.bukkit.event.block.BlockBreakEvent");
            }
        };
    }
}
