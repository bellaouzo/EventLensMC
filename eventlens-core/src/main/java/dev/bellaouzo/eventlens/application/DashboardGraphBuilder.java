package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraph;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraphEdge;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardGraphNode;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionExportBundle;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class DashboardGraphBuilder {

    static final int MAX_EVENT_GRAPH_EDGES = 512;
    static final int MAX_PLUGIN_GRAPH_NODES = 64;

    private DashboardGraphBuilder() {}

    public static DashboardGraph buildEventRelationshipGraph(ListenerRegistryPort listenerRegistryPort) {
        Map<String, DashboardGraphNode> nodes = new LinkedHashMap<>();
        List<DashboardGraphEdge> edges = new ArrayList<>();
        boolean truncated = false;

        List<String> eventClassNames = listenerRegistryPort.listKnownEventClassNames();
        for (String eventClassName : eventClassNames) {
            String eventId = eventNodeId(eventClassName);
            nodes.putIfAbsent(
                    eventId,
                    new DashboardGraphNode(eventId, simpleName(eventClassName), DashboardGraphNode.Kind.EVENT, 0));

            List<ListenerRegistration> listeners = listenerRegistryPort.getListeners(eventClassName);
            for (ListenerRegistration registration : listeners) {
                if (edges.size() >= MAX_EVENT_GRAPH_EDGES) {
                    truncated = true;
                    break;
                }
                String pluginId = pluginNodeId(registration.pluginName());
                nodes.putIfAbsent(
                        pluginId,
                        new DashboardGraphNode(pluginId, registration.pluginName(), DashboardGraphNode.Kind.PLUGIN, 0));
                edges.add(new DashboardGraphEdge(
                        pluginId, eventId, 1, registration.priority() + " · " + registration.methodName()));
            }
            if (truncated) {
                break;
            }
        }

        return finalizeGraph("Event relationship graph", nodes, edges, truncated);
    }

    public static DashboardGraph buildPluginInteractionGraph(
            ListenerRegistryPort listenerRegistryPort,
            TraceSessionManager traceSessionManager,
            Optional<String> sessionId) {
        Map<String, Integer> sharedEventCounts = new HashMap<>();

        for (String eventClassName : listenerRegistryPort.listKnownEventClassNames()) {
            Set<String> pluginsOnEvent = new HashSet<>();
            for (ListenerRegistration registration : listenerRegistryPort.getListeners(eventClassName)) {
                pluginsOnEvent.add(registration.pluginName());
            }
            if (pluginsOnEvent.size() < 2) {
                continue;
            }
            List<String> sorted = pluginsOnEvent.stream()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            for (int left = 0; left < sorted.size(); left++) {
                for (int right = left + 1; right < sorted.size(); right++) {
                    String key = interactionKey(sorted.get(left), sorted.get(right));
                    sharedEventCounts.merge(key, 1, Integer::sum);
                }
            }
        }

        Map<String, Integer> traceCoDispatchCounts = new HashMap<>();
        if (sessionId.isPresent()) {
            traceSessionManager
                    .getExportBundle(sessionId.get())
                    .ifPresent(bundle -> accumulateTraceCoDispatches(bundle, traceCoDispatchCounts));
        }

        Map<String, DashboardGraphNode> nodes = new LinkedHashMap<>();
        Map<String, Integer> nodeWeights = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sharedEventCounts.forEach((key, count) -> mergeInteractionParts(nodeWeights, key, count));
        traceCoDispatchCounts.forEach((key, count) -> mergeInteractionParts(nodeWeights, key, count));

        List<Map.Entry<String, Integer>> rankedPlugins = nodeWeights.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)))
                .limit(MAX_PLUGIN_GRAPH_NODES)
                .toList();
        boolean truncated = nodeWeights.size() > MAX_PLUGIN_GRAPH_NODES;
        Set<String> includedPlugins =
                rankedPlugins.stream().map(Map.Entry::getKey).collect(Collectors.toSet());

        for (Map.Entry<String, Integer> entry : rankedPlugins) {
            String pluginId = pluginNodeId(entry.getKey());
            nodes.put(
                    pluginId,
                    new DashboardGraphNode(pluginId, entry.getKey(), DashboardGraphNode.Kind.PLUGIN, entry.getValue()));
        }

        List<DashboardGraphEdge> edges = new ArrayList<>();
        appendInteractionEdges(sharedEventCounts, traceCoDispatchCounts, includedPlugins, edges);

        String title = sessionId
                .map(id -> "Plugin interaction graph (session " + id + ")")
                .orElse("Plugin interaction graph");
        return finalizeGraph(title, nodes, edges, truncated);
    }

    private static void accumulateTraceCoDispatches(
            TraceSessionExportBundle bundle, Map<String, Integer> traceCoDispatchCounts) {
        for (TraceDispatchRecord dispatch : bundle.records()) {
            Set<String> plugins = new HashSet<>();
            for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
                plugins.add(timing.pluginName());
            }
            if (plugins.size() < 2) {
                continue;
            }
            List<String> sorted =
                    plugins.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
            for (int left = 0; left < sorted.size(); left++) {
                for (int right = left + 1; right < sorted.size(); right++) {
                    traceCoDispatchCounts.merge(interactionKey(sorted.get(left), sorted.get(right)), 1, Integer::sum);
                }
            }
        }
    }

    private static void appendInteractionEdges(
            Map<String, Integer> sharedEventCounts,
            Map<String, Integer> traceCoDispatchCounts,
            Set<String> includedPlugins,
            List<DashboardGraphEdge> edges) {
        Set<String> seen = new HashSet<>();
        sharedEventCounts.forEach((key, sharedEvents) -> {
            InteractionPair pair = parseInteractionKey(key);
            if (!includedPlugins.contains(pair.left()) || !includedPlugins.contains(pair.right())) {
                return;
            }
            int traceDispatches = traceCoDispatchCounts.getOrDefault(key, 0);
            int weight = sharedEvents + traceDispatches;
            if (weight <= 0 || !seen.add(key)) {
                return;
            }
            edges.add(new DashboardGraphEdge(
                    pluginNodeId(pair.left()),
                    pluginNodeId(pair.right()),
                    weight,
                    sharedEvents + " shared events"
                            + (traceDispatches > 0 ? ", " + traceDispatches + " co-dispatches" : "")));
        });
        traceCoDispatchCounts.forEach((key, traceDispatches) -> {
            if (sharedEventCounts.containsKey(key) || !seen.add(key)) {
                return;
            }
            InteractionPair pair = parseInteractionKey(key);
            if (!includedPlugins.contains(pair.left()) || !includedPlugins.contains(pair.right())) {
                return;
            }
            edges.add(new DashboardGraphEdge(
                    pluginNodeId(pair.left()),
                    pluginNodeId(pair.right()),
                    traceDispatches,
                    traceDispatches + " co-dispatches"));
        });
    }

    private static void mergeInteractionParts(Map<String, Integer> nodeWeights, String key, int count) {
        InteractionPair pair = parseInteractionKey(key);
        nodeWeights.merge(pair.left(), count, Integer::sum);
        nodeWeights.merge(pair.right(), count, Integer::sum);
    }

    private static InteractionPair parseInteractionKey(String key) {
        int separator = key.indexOf('\0');
        if (separator < 0) {
            return new InteractionPair(key, key);
        }
        return new InteractionPair(key.substring(0, separator), key.substring(separator + 1));
    }

    private record InteractionPair(String left, String right) {}

    private static DashboardGraph finalizeGraph(
            String title, Map<String, DashboardGraphNode> nodes, List<DashboardGraphEdge> edges, boolean truncated) {
        List<DashboardGraphNode> nodeList = nodes.values().stream()
                .sorted(Comparator.comparing(DashboardGraphNode::label, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new DashboardGraph(title, nodeList, List.copyOf(edges), truncated);
    }

    static String eventNodeId(String eventClassName) {
        return "event:" + eventClassName;
    }

    static String pluginNodeId(String pluginName) {
        return "plugin:" + pluginName;
    }

    private static String interactionKey(String left, String right) {
        if (left.compareToIgnoreCase(right) <= 0) {
            return left + "\0" + right;
        }
        return right + "\0" + left;
    }

    private static String simpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
}
