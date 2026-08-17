package dev.bellaouzo.eventlens.domain.dashboard;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record DashboardGraph(
        @NonNull String title,
        @NonNull List<DashboardGraphNode> nodes,
        @NonNull List<DashboardGraphEdge> edges,
        boolean truncated) {

    public DashboardGraph {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
    }
}
