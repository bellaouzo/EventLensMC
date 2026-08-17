package dev.bellaouzo.eventlens.domain.report;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraph;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraphEdge;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraphNode;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardServerContext;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardStatusPayload;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardJsonSerializerTest {

    @Test
    void serializesStatusPayload() {
        DashboardServerContext server =
                new DashboardServerContext("Paper 26.2", "1.0.0", "world", "survival", 3, 19.8, 1_700_000_000_000L);

        String json = DashboardJsonSerializer.serializeStatus(new DashboardStatusPayload(
                true, 8765, "127.0.0.1", true, 2, server, "abc12345", 1_000L, 4, "PlayerInteractEvent"));

        assertTrue(json.contains("\"paperVersion\": \"Paper 26.2\""));
        assertTrue(json.contains("\"defaultWorldName\": \"world\""));
        assertTrue(json.contains("\"activeTraceSessionId\": \"abc12345\""));
        assertTrue(json.contains("\"tps\": 19.8"));
    }

    @Test
    void serializesGraphPayload() {
        DashboardGraph graph = new DashboardGraph(
                "Test graph",
                List.of(new DashboardGraphNode("plugin:A", "A", DashboardGraphNode.Kind.PLUGIN, 3)),
                List.of(new DashboardGraphEdge("plugin:A", "event:B", 2, "shared")),
                false);

        String json = DashboardJsonSerializer.serializeGraph(graph);
        assertTrue(json.contains("\"type\": \"graph\""));
        assertTrue(json.contains("\"plugin:A\""));
        assertTrue(json.contains("shared"));
    }
}
