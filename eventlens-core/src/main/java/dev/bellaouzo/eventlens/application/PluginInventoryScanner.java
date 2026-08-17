package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerBinding;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerInventory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

final class PluginInventoryScanner {

    private PluginInventoryScanner() {}

    static PluginListenerInventory scan(String pluginName, ListenerRegistryPort listenerRegistryPort) {
        List<PluginListenerBinding> bindings = new ArrayList<>();
        Map<String, Integer> countByEvent = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Integer> countByPriority = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Integer> coInteractions = new HashMap<>();
        Set<String> eventClassNames = new HashSet<>();

        for (String eventClassName : listenerRegistryPort.listKnownEventClassNames()) {
            List<ListenerRegistration> listeners = listenerRegistryPort.getListeners(eventClassName);
            boolean pluginPresent = false;
            Set<String> otherPluginsOnEvent = new HashSet<>();

            for (ListenerRegistration registration : listeners) {
                if (registration.pluginName().equalsIgnoreCase(pluginName)) {
                    pluginPresent = true;
                    bindings.add(new PluginListenerBinding(eventClassName, registration));
                    String simpleName = simpleEventName(eventClassName);
                    countByEvent.merge(simpleName, 1, Integer::sum);
                    countByPriority.merge(registration.priority(), 1, Integer::sum);
                    eventClassNames.add(eventClassName);
                } else {
                    otherPluginsOnEvent.add(registration.pluginName());
                }
            }

            if (pluginPresent) {
                for (String otherPlugin : otherPluginsOnEvent) {
                    coInteractions.merge(otherPlugin, 1, Integer::sum);
                }
            }
        }

        bindings.sort(Comparator.comparing(PluginListenerBinding::eventClassName)
                .thenComparing(binding -> binding.registration().registrationOrder()));

        List<String> sortedEvents =
                eventClassNames.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();

        return new PluginListenerInventory(
                pluginName,
                List.copyOf(bindings),
                sortedEvents,
                Map.copyOf(countByEvent),
                Map.copyOf(countByPriority),
                Map.copyOf(coInteractions));
    }

    static List<String> eventsOnlyIn(List<String> source, List<String> other) {
        Set<String> otherSet = new HashSet<>(other);
        return source.stream()
                .filter(event -> !otherSet.contains(event))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    static List<String> sharedEvents(List<String> left, List<String> right) {
        Set<String> rightSet = new HashSet<>(right);
        return left.stream()
                .filter(rightSet::contains)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    static List<String> sharedCoPlugins(List<PluginListenerInventory> inventories) {
        if (inventories.isEmpty()) {
            return List.of();
        }

        Set<String> shared =
                new HashSet<>(inventories.getFirst().registryCoInteractions().keySet());
        for (int index = 1; index < inventories.size(); index++) {
            shared.retainAll(inventories.get(index).registryCoInteractions().keySet());
        }

        return shared.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    private static String simpleEventName(String eventClassName) {
        int lastDot = eventClassName.lastIndexOf('.');
        return lastDot >= 0 ? eventClassName.substring(lastDot + 1) : eventClassName;
    }
}
