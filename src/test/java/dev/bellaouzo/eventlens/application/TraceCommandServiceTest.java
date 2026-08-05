package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceStartResult;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraceCommandServiceTest {

    @Test
    void startTraceRejectsUnsupportedEvent() {
        StubListenerRegistry registry = new StubListenerRegistry();
        registry.setSearchResult(EventSearchResult.found("org.bukkit.event.player.PlayerRespawnEvent"));
        TraceCommandService service =
                new TraceCommandService(new TraceSessionManager(), registry, new NoOpTraceHookPort());

        TraceStartResult result = service.startTrace("PlayerRespawnEvent", "admin", unrestrictedOptions());

        TraceStartResult.Failure failure = assertInstanceOf(TraceStartResult.Failure.class, result);
        assertEquals(TraceStartResult.Failure.Reason.UNSUPPORTED_EVENT, failure.reason());
    }

    @Test
    void startTraceRequiresHotEventConfirmationWhenConfigured() {
        StubListenerRegistry registry = new StubListenerRegistry();
        registry.setSearchResult(EventSearchResult.found("org.bukkit.event.player.PlayerMoveEvent"));
        EventLensCommandConfig config = new EventLensCommandConfig(
                false,
                EventLensCommandConfig.defaults().defaultDetailLevel(),
                EventLensCommandConfig.defaults().defaultSlowThresholdNanos(),
                true,
                true,
                20,
                32,
                EventLensCommandConfig.defaults().presets());
        TraceCommandService service =
                new TraceCommandService(new TraceSessionManager(), registry, new NoOpTraceHookPort(), config);

        TraceStartResult result = service.startTrace(
                "PlayerMoveEvent",
                "admin",
                new TraceCommandService.TraceStartOptions(
                        TraceFilter.Builder.unrestricted()
                                .pluginName("EventLens")
                                .build(),
                        Optional.empty(),
                        Optional.empty(),
                        config.defaultSlowThresholdNanos(),
                        false,
                        false,
                        Optional.empty()));

        TraceStartResult.Failure failure = assertInstanceOf(TraceStartResult.Failure.class, result);
        assertEquals(TraceStartResult.Failure.Reason.HOT_EVENT_CONFIRMATION, failure.reason());
        assertTrue(failure.confirmCommand().isPresent());
    }

    @Test
    void listSupportedEventSimpleNamesMatchesRegistry() {
        TraceCommandService service =
                new TraceCommandService(new TraceSessionManager(), new StubListenerRegistry(), new NoOpTraceHookPort());

        assertEquals(13, service.listSupportedEventSimpleNames().size());
        assertTrueContains(service.listSupportedEventSimpleNames(), "BlockBreakEvent");
    }

    private static void assertTrueContains(List<String> values, String expected) {
        if (!values.contains(expected)) {
            throw new AssertionError("Expected list to contain " + expected + " but was " + values);
        }
    }

    private static TraceCommandService.TraceStartOptions unrestrictedOptions() {
        return TraceCommandService.TraceStartOptions.parse(List.of());
    }

    private static final class StubListenerRegistry implements ListenerRegistryPort {
        private EventSearchResult searchResult = EventSearchResult.notFound();

        void setSearchResult(EventSearchResult searchResult) {
            this.searchResult = searchResult;
        }

        @Override
        public EventSearchResult searchEvents(String query) {
            return searchResult;
        }

        @Override
        public List<ListenerRegistration> getListeners(String eventClassName) {
            return List.of();
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

    private static final class NoOpTraceHookPort implements TraceHookPort {
        @Override
        public void registerHooksForEvent(String eventClassName) {
            // Test stub: hook registration is not required for service unit tests.
        }

        @Override
        public void syncWithActiveSessions(TraceSessionManager traceSessionManager) {
            // Test stub: hook sync is not required for service unit tests.
        }
    }
}
