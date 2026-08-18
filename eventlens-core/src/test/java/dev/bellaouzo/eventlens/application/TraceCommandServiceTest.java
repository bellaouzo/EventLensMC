package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.domain.trace.TraceStartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceStopResult;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraceCommandServiceTest {

    @Test
    void startTraceRejectsUnsupportedEvent() {
        StubListenerRegistry registry = new StubListenerRegistry();
        registry.setSearchResult(EventSearchResult.found("org.bukkit.event.server.PluginEnableEvent"));
        TraceCommandService service =
                new TraceCommandService(new TraceSessionManager(), registry, new NoOpTraceHookPort());

        TraceStartResult result = service.startTrace("PluginEnableEvent", "admin", unrestrictedOptions());

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
                EventLensCommandConfig.defaults().presets(),
                java.util.Optional.empty());
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
    void startTraceRegistersHooksForEveryCommaSeparatedEvent() {
        QueryListenerRegistry registry = new QueryListenerRegistry();
        registry.put("PlayerInteractEvent", "org.bukkit.event.player.PlayerInteractEvent");
        registry.put("BlockBreakEvent", "org.bukkit.event.block.BlockBreakEvent");
        RecordingTraceHookPort hooks = new RecordingTraceHookPort();
        TraceSessionManager manager = new TraceSessionManager();
        TraceCommandService service = new TraceCommandService(manager, registry, hooks);

        TraceStartResult result =
                service.startTrace("PlayerInteractEvent,BlockBreakEvent", "admin", unrestrictedOptions());

        TraceStartResult.Success success = assertInstanceOf(TraceStartResult.Success.class, result);
        assertEquals(2, success.eventClassNames().size());
        assertTrue(hooks.registered.contains("org.bukkit.event.player.PlayerInteractEvent"));
        assertTrue(hooks.registered.contains("org.bukkit.event.block.BlockBreakEvent"));
        assertEquals(2, manager.listSessions().getFirst().eventClassNames().size());
    }

    @Test
    void stopTraceStopsOnlyTheRequestedSession() {
        TraceSessionManager manager = new TraceSessionManager();
        TraceCommandService service =
                new TraceCommandService(manager, new StubListenerRegistry(), new NoOpTraceHookPort());
        TraceSessionConfig config = new TraceSessionConfig(
                "org.bukkit.event.block.BlockBreakEvent",
                TraceFilter.Builder.unrestricted().build(),
                Optional.empty(),
                Optional.empty());
        long nowMillis = System.currentTimeMillis();
        String keep = manager.startSession(config, "admin", nowMillis);
        String stop = manager.startSession(config, "admin", nowMillis);

        TraceStopResult result = service.stopSession(stop);

        TraceStopResult.Success success = assertInstanceOf(TraceStopResult.Success.class, result);
        assertEquals(List.of(stop), success.stoppedSessionIds());
        assertEquals(1, service.listOpenSessionIds().size());
        assertEquals(keep, service.listOpenSessionIds().getFirst());
    }

    @Test
    void stopTraceReportsMissingSession() {
        TraceCommandService service =
                new TraceCommandService(new TraceSessionManager(), new StubListenerRegistry(), new NoOpTraceHookPort());

        TraceStopResult result = service.stopSession("missing");

        TraceStopResult.NotFound notFound = assertInstanceOf(TraceStopResult.NotFound.class, result);
        assertEquals("missing", notFound.sessionId());
    }

    @Test
    void listSupportedEventSimpleNamesMatchesRegistry() {
        TraceCommandService service =
                new TraceCommandService(new TraceSessionManager(), new StubListenerRegistry(), new NoOpTraceHookPort());

        assertEquals(142, service.listSupportedEventSimpleNames().size());
        assertTrueContains(service.listSupportedEventSimpleNames(), "BlockBreakEvent");
        assertTrueContains(service.listSupportedEventSimpleNames(), "EntityExplodeEvent");
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

    private static final class QueryListenerRegistry implements ListenerRegistryPort {
        private final Map<String, String> classNames = new HashMap<>();

        void put(String query, String className) {
            classNames.put(query.toLowerCase(java.util.Locale.ROOT), className);
        }

        @Override
        public EventSearchResult searchEvents(String query) {
            String className = classNames.get(query.toLowerCase(java.util.Locale.ROOT));
            return className == null ? EventSearchResult.notFound() : EventSearchResult.found(className);
        }

        @Override
        public List<ListenerRegistration> getListeners(String eventClassName) {
            return List.of();
        }

        @Override
        public List<String> listKnownEventSimpleNames() {
            return List.of();
        }

        @Override
        public List<String> listKnownEventClassNames() {
            return List.copyOf(classNames.values());
        }
    }

    private static final class RecordingTraceHookPort implements TraceHookPort {
        private final List<String> registered = new ArrayList<>();

        @Override
        public void registerHooksForEvent(String eventClassName) {
            registered.add(eventClassName);
        }

        @Override
        public void syncWithActiveSessions(TraceSessionManager traceSessionManager) {
            // Test stub: registration list is the assertion target.
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
