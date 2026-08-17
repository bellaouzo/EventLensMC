package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.PluginRegistryPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.plugin.PluginDescriptor;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerQueryResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginQueryResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginSearchResult;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PluginQueryServiceTest {

    @Test
    void queryProfileReturnsNotFoundForUnknownPlugin() {
        PluginQueryService service = new PluginQueryService(
                new StubPluginRegistry(), new StubListenerRegistry(), new TraceSessionManager(), null);

        PluginQueryResult result = service.queryProfile("MissingPlugin");

        assertInstanceOf(PluginQueryResult.NotFound.class, result);
    }

    @Test
    void queryProfileBuildsInventoryForPlugin() {
        StubPluginRegistry pluginRegistry = new StubPluginRegistry();
        pluginRegistry.setSearchResult(PluginSearchResult.found("WorldGuard"));
        pluginRegistry.setDescriptor(new PluginDescriptor(
                "WorldGuard", "7.0.0", true, List.of("WorldEdit"), List.of("Vault"), List.of(), List.of()));

        StubListenerRegistry listenerRegistry = new StubListenerRegistry();
        listenerRegistry.setListenersByEvent(Map.of(
                "org.bukkit.event.block.BlockBreakEvent",
                List.of(registration(1, "WorldGuard", "NORMAL"), registration(2, "Essentials", "NORMAL")),
                "org.bukkit.event.player.PlayerJoinEvent",
                List.of(registration(1, "WorldGuard", "HIGHEST"))));
        listenerRegistry.setEventClassNames(
                List.of("org.bukkit.event.block.BlockBreakEvent", "org.bukkit.event.player.PlayerJoinEvent"));

        PluginQueryService service =
                new PluginQueryService(pluginRegistry, listenerRegistry, new TraceSessionManager(), null);

        PluginQueryResult result = service.queryProfile("WorldGuard");

        assertInstanceOf(PluginQueryResult.Success.class, result);
        PluginQueryResult.Success success = (PluginQueryResult.Success) result;
        assertEquals(2, success.profile().inventory().eventClassNames().size());
        assertEquals(2, success.profile().inventory().bindings().size());
        assertEquals(1, success.profile().coInteractions().size());
        assertEquals("Essentials", success.profile().coInteractions().getFirst().pluginName());
    }

    @Test
    void queryListenersPaginatesPluginBindings() {
        StubPluginRegistry pluginRegistry = new StubPluginRegistry();
        pluginRegistry.setSearchResult(PluginSearchResult.found("PluginA"));

        List<ListenerRegistration> listeners = new ArrayList<>();
        for (int index = 1; index <= 8; index++) {
            listeners.add(registration(index, "PluginA", "NORMAL"));
        }

        StubListenerRegistry listenerRegistry = new StubListenerRegistry();
        listenerRegistry.setListenersByEvent(Map.of("org.bukkit.event.player.PlayerJoinEvent", listeners));
        listenerRegistry.setEventClassNames(List.of("org.bukkit.event.player.PlayerJoinEvent"));

        PluginQueryService service =
                new PluginQueryService(pluginRegistry, listenerRegistry, new TraceSessionManager(), null);

        PluginListenerQueryResult pageOne = service.queryListeners("PluginA", null, 1, 6);
        PluginListenerQueryResult pageTwo = service.queryListeners("PluginA", null, 2, 6);

        assertInstanceOf(PluginListenerQueryResult.Success.class, pageOne);
        PluginListenerQueryResult.Success successOne = (PluginListenerQueryResult.Success) pageOne;
        assertEquals(6, successOne.page().bindings().size());
        assertEquals(2, successOne.page().totalPages());

        assertInstanceOf(PluginListenerQueryResult.Success.class, pageTwo);
        PluginListenerQueryResult.Success successTwo = (PluginListenerQueryResult.Success) pageTwo;
        assertEquals(2, successTwo.page().bindings().size());
    }

    @Test
    void comparePluginsReportsSharedEvents() {
        StubPluginRegistry pluginRegistry = new StubPluginRegistry();
        pluginRegistry.setSearchResults(Map.of(
                "PluginA", PluginSearchResult.found("PluginA"),
                "PluginB", PluginSearchResult.found("PluginB")));
        pluginRegistry.setDescriptor(
                new PluginDescriptor("PluginA", "1.0", true, List.of(), List.of(), List.of(), List.of()));
        pluginRegistry.setDescriptor(
                new PluginDescriptor("PluginB", "1.0", true, List.of(), List.of(), List.of(), List.of()));

        StubListenerRegistry listenerRegistry = new StubListenerRegistry();
        listenerRegistry.setListenersByEvent(Map.of(
                "org.bukkit.event.player.PlayerJoinEvent",
                List.of(registration(1, "PluginA", "NORMAL"), registration(2, "PluginB", "NORMAL")),
                "org.bukkit.event.block.BlockBreakEvent",
                List.of(registration(1, "PluginA", "NORMAL"))));
        listenerRegistry.setEventClassNames(
                List.of("org.bukkit.event.player.PlayerJoinEvent", "org.bukkit.event.block.BlockBreakEvent"));

        PluginQueryService service =
                new PluginQueryService(pluginRegistry, listenerRegistry, new TraceSessionManager(), null);

        var compare = service.comparePlugins("PluginA", "PluginB");

        assert compare.isPresent();
        assertEquals(1, compare.get().sharedEvents().size());
        assertEquals(1, compare.get().leftOnlyEvents().size());
        assertEquals(0, compare.get().rightOnlyEvents().size());
    }

    private static ListenerRegistration registration(int order, String pluginName, String priority) {
        return new ListenerRegistration(order, pluginName, "com.example.Listener", "onEvent", priority, false);
    }

    private static final class StubPluginRegistry implements PluginRegistryPort {

        private PluginSearchResult searchResult = PluginSearchResult.notFound();
        private final Map<String, PluginSearchResult> searchResults = new HashMap<>();
        private final Map<String, PluginDescriptor> descriptors = new HashMap<>();

        void setSearchResult(PluginSearchResult searchResult) {
            this.searchResult = searchResult;
        }

        void setSearchResults(Map<String, PluginSearchResult> searchResults) {
            this.searchResults.putAll(searchResults);
        }

        void setDescriptor(PluginDescriptor descriptor) {
            descriptors.put(descriptor.name(), descriptor);
        }

        @Override
        public PluginSearchResult searchPlugins(String query) {
            return searchResults.getOrDefault(query, searchResult);
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

        private Map<String, List<ListenerRegistration>> listenersByEvent = Map.of();
        private List<String> eventClassNames = List.of();

        void setListenersByEvent(Map<String, List<ListenerRegistration>> listenersByEvent) {
            this.listenersByEvent = listenersByEvent;
        }

        void setEventClassNames(List<String> eventClassNames) {
            this.eventClassNames = eventClassNames;
        }

        @Override
        public EventSearchResult searchEvents(String query) {
            return EventSearchResult.notFound();
        }

        @Override
        public List<ListenerRegistration> getListeners(String eventClassName) {
            return listenersByEvent.getOrDefault(eventClassName, List.of());
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
