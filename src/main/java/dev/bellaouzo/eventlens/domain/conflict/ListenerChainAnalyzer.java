package dev.bellaouzo.eventlens.domain.conflict;

import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ListenerChainAnalyzer {

    private ListenerChainAnalyzer() {}

    public static List<DispatchConflict> detectChainIssues(List<TraceListenerSnapshot> chain) {
        return analyzeEntries(fromSnapshots(chain));
    }

    public static List<DispatchConflict> detectInventoryIssues(List<ListenerRegistration> registrations) {
        return analyzeEntries(fromRegistrations(registrations));
    }

    private static List<DispatchConflict> analyzeEntries(List<ListenerEntry> entries) {
        List<DispatchConflict> conflicts = new ArrayList<>();
        Set<String> seenExact = new LinkedHashSet<>();
        Map<String, List<ListenerEntry>> byPluginPriority = new LinkedHashMap<>();

        for (ListenerEntry entry : entries) {
            String exactKey = entry.pluginName() + "|" + entry.listenerClassName() + "|" + entry.methodName() + "|"
                    + entry.priority();
            if (!seenExact.add(exactKey)) {
                conflicts.add(new DispatchConflict(
                        ConflictKind.DUPLICATE_REGISTRATION,
                        ConflictSeverity.MEDIUM,
                        "Duplicate registration: " + entry.displayName() + " at " + entry.priority(),
                        List.of(entry.pluginName()),
                        java.util.Optional.empty()));
            }

            String groupKey = entry.pluginName() + "|" + entry.priority();
            byPluginPriority
                    .computeIfAbsent(groupKey, ignored -> new ArrayList<>())
                    .add(entry);
        }

        for (Map.Entry<String, List<ListenerEntry>> group : byPluginPriority.entrySet()) {
            List<ListenerEntry> groupEntries = group.getValue();
            if (groupEntries.size() >= 2) {
                Set<String> distinctListeners = new LinkedHashSet<>();
                for (ListenerEntry entry : groupEntries) {
                    distinctListeners.add(entry.listenerClassName() + "|" + entry.methodName());
                }
                if (distinctListeners.size() >= 2) {
                    ListenerEntry first = groupEntries.getFirst();
                    String names = groupEntries.stream()
                            .map(ListenerEntry::shortDisplay)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                    conflicts.add(new DispatchConflict(
                            ConflictKind.EQUIVALENT_LISTENER,
                            ConflictSeverity.LOW,
                            "Plugin " + first.pluginName() + " registered " + groupEntries.size() + " listeners at "
                                    + first.priority() + ": " + names,
                            List.of(first.pluginName()),
                            java.util.Optional.empty()));
                }
            }
        }

        return List.copyOf(conflicts);
    }

    private static List<ListenerEntry> fromSnapshots(List<TraceListenerSnapshot> chain) {
        return chain.stream()
                .map(listener -> new ListenerEntry(
                        listener.pluginName(),
                        listener.listenerClassName(),
                        listener.methodName(),
                        listener.priority()))
                .toList();
    }

    private static List<ListenerEntry> fromRegistrations(List<ListenerRegistration> registrations) {
        return registrations.stream()
                .map(listener -> new ListenerEntry(
                        listener.pluginName(),
                        listener.listenerClassName(),
                        listener.methodName(),
                        listener.priority()))
                .toList();
    }

    private record ListenerEntry(String pluginName, String listenerClassName, String methodName, String priority) {

        String displayName() {
            return pluginName + "/" + shortClassName() + "#" + methodName;
        }

        String shortDisplay() {
            return shortClassName() + "#" + methodName;
        }

        private String shortClassName() {
            int lastDot = listenerClassName.lastIndexOf('.');
            return lastDot >= 0 ? listenerClassName.substring(lastDot + 1) : listenerClassName;
        }
    }
}
