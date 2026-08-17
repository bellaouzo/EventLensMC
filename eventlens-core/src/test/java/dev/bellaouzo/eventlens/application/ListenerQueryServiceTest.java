package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerInventoryResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListenerQueryServiceTest {

    @Test
    void queryListenersReturnsNotFoundForUnknownEvent() {
        ListenerQueryService service = new ListenerQueryService(new StubListenerRegistry());

        ListenerInventoryResult result = service.queryListeners("MissingEvent", 1);

        assertInstanceOf(ListenerInventoryResult.NotFound.class, result);
    }

    @Test
    void queryListenersReturnsAmbiguousMatches() {
        StubListenerRegistry registry = new StubListenerRegistry();
        registry.setSearchResult(EventSearchResult.ambiguous(
                List.of("org.bukkit.event.player.PlayerJoinEvent", "org.bukkit.event.player.PlayerQuitEvent")));
        ListenerQueryService service = new ListenerQueryService(registry);

        ListenerInventoryResult result = service.queryListeners("Player", 1);

        assertInstanceOf(ListenerInventoryResult.Ambiguous.class, result);
        ListenerInventoryResult.Ambiguous ambiguous = (ListenerInventoryResult.Ambiguous) result;
        assertEquals(2, ambiguous.candidateClassNames().size());
    }

    @Test
    void queryListenersPaginatesResults() {
        StubListenerRegistry registry = new StubListenerRegistry();
        registry.setSearchResult(EventSearchResult.found("org.bukkit.event.player.PlayerJoinEvent"));
        registry.setListeners(List.of(
                registration(1, "PluginA"),
                registration(2, "PluginB"),
                registration(3, "PluginC"),
                registration(4, "PluginD"),
                registration(5, "PluginE"),
                registration(6, "PluginF"),
                registration(7, "PluginG"),
                registration(8, "PluginH"),
                registration(9, "PluginI"),
                registration(10, "PluginJ"),
                registration(11, "PluginK")));

        ListenerQueryService service = new ListenerQueryService(registry);

        ListenerInventoryResult pageOne = service.queryListeners("PlayerJoinEvent", 1);
        ListenerInventoryResult pageTwo = service.queryListeners("PlayerJoinEvent", 2);

        assertInstanceOf(ListenerInventoryResult.Success.class, pageOne);
        ListenerInventoryResult.Success successOne = (ListenerInventoryResult.Success) pageOne;
        assertEquals(6, successOne.page().listeners().size());
        assertEquals(2, successOne.page().totalPages());
        assertEquals(11, successOne.page().totalListeners());

        assertInstanceOf(ListenerInventoryResult.Success.class, pageTwo);
        ListenerInventoryResult.Success successTwo = (ListenerInventoryResult.Success) pageTwo;
        assertEquals(5, successTwo.page().listeners().size());
        assertEquals("PluginG", successTwo.page().listeners().getFirst().pluginName());
    }

    @Test
    void queryListenersRejectsInvalidPage() {
        StubListenerRegistry registry = new StubListenerRegistry();
        registry.setSearchResult(EventSearchResult.found("org.bukkit.event.player.PlayerJoinEvent"));
        registry.setListeners(List.of(registration(1, "PluginA")));
        ListenerQueryService service = new ListenerQueryService(registry);

        ListenerInventoryResult result = service.queryListeners("PlayerJoinEvent", 3);

        assertInstanceOf(ListenerInventoryResult.InvalidPage.class, result);
        ListenerInventoryResult.InvalidPage invalidPage = (ListenerInventoryResult.InvalidPage) result;
        assertEquals(3, invalidPage.requestedPage());
        assertEquals(1, invalidPage.totalPages());
    }

    private static ListenerRegistration registration(int order, String pluginName) {
        return new ListenerRegistration(order, pluginName, "com.example.Listener", "onEvent", "NORMAL", false);
    }

    private static final class StubListenerRegistry implements ListenerRegistryPort {

        private EventSearchResult searchResult = EventSearchResult.notFound();
        private List<ListenerRegistration> listeners = List.of();

        void setSearchResult(EventSearchResult searchResult) {
            this.searchResult = searchResult;
        }

        void setListeners(List<ListenerRegistration> listeners) {
            this.listeners = new ArrayList<>(listeners);
        }

        @Override
        public EventSearchResult searchEvents(String query) {
            return searchResult;
        }

        @Override
        public List<ListenerRegistration> getListeners(String eventClassName) {
            return List.copyOf(listeners);
        }

        @Override
        public List<String> listKnownEventSimpleNames() {
            return List.of("PlayerJoinEvent");
        }

        @Override
        public List<String> listKnownEventClassNames() {
            return List.of("org.bukkit.event.player.PlayerJoinEvent");
        }
    }
}
