package dev.bellaouzo.eventlens.command.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.PluginQueryService;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.PluginRegistryPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.plugin.PluginDescriptor;
import dev.bellaouzo.eventlens.domain.plugin.PluginSearchResult;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PluginCommandTabCompleterTest {

    @Test
    void completesListenersSubcommandPartially() {
        PluginQueryService service = stubService();
        List<String> suggestions =
                PluginCommandTabCompleter.complete(service, new String[] {"plugin", "EventLens", "lis"}, "lis");
        assertEquals(List.of("listeners"), suggestions);
    }

    @Test
    void completesKnownEventsAfterListenersSubcommand() {
        PluginQueryService service = stubService();
        List<String> suggestions = PluginCommandTabCompleter.complete(
                service, new String[] {"plugin", "EventLens", "listeners", "PlayerJ"}, "PlayerJ");
        assertTrue(suggestions.contains("PlayerJoinEvent"));
    }

    @Test
    void completesLoadedPluginNamesForCompare() {
        PluginQueryService service = stubService();
        List<String> suggestions =
                PluginCommandTabCompleter.complete(service, new String[] {"plugin", "compare", "Event"}, "Event");
        assertEquals(List.of("EventLens"), suggestions);
    }

    private static PluginQueryService stubService() {
        StubPluginRegistry pluginRegistry = new StubPluginRegistry();
        pluginRegistry.setDescriptor(
                new PluginDescriptor("EventLens", "1.0.0", true, List.of(), List.of(), List.of(), List.of()));
        pluginRegistry.setSearchResult(PluginSearchResult.found("EventLens"));

        StubListenerRegistry listenerRegistry = new StubListenerRegistry();
        listenerRegistry.setEventClassNames(List.of("org.bukkit.event.player.PlayerJoinEvent"));

        return new PluginQueryService(pluginRegistry, listenerRegistry, new TraceSessionManager(), null);
    }

    private static final class StubPluginRegistry implements PluginRegistryPort {

        private PluginSearchResult searchResult = PluginSearchResult.notFound();
        private final java.util.Map<String, PluginDescriptor> descriptors = new java.util.HashMap<>();

        void setSearchResult(PluginSearchResult searchResult) {
            this.searchResult = searchResult;
        }

        void setDescriptor(PluginDescriptor descriptor) {
            descriptors.put(descriptor.name(), descriptor);
        }

        @Override
        public PluginSearchResult searchPlugins(String query) {
            if ("EventLens".equalsIgnoreCase(query) || "Event".equalsIgnoreCase(query)) {
                return PluginSearchResult.found("EventLens");
            }
            return searchResult;
        }

        @Override
        public Optional<PluginDescriptor> getDescriptor(String pluginName) {
            return Optional.ofNullable(descriptors.get(pluginName));
        }

        @Override
        public List<String> listPluginNames() {
            return List.copyOf(descriptors.keySet());
        }
    }

    private static final class StubListenerRegistry implements ListenerRegistryPort {

        private List<String> eventClassNames = List.of();

        void setEventClassNames(List<String> eventClassNames) {
            this.eventClassNames = eventClassNames;
        }

        @Override
        public EventSearchResult searchEvents(String query) {
            return EventSearchResult.notFound();
        }

        @Override
        public List<ListenerRegistration> getListeners(String eventClassName) {
            return List.of();
        }

        @Override
        public List<String> listKnownEventSimpleNames() {
            return eventClassNames.stream()
                    .map(name -> name.substring(name.lastIndexOf('.') + 1))
                    .toList();
        }

        @Override
        public List<String> listKnownEventClassNames() {
            return eventClassNames;
        }
    }
}
