package dev.bellaouzo.eventlens.command.trace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraceLiveTabCompleterTest {

    @Test
    void completesSessionIdsAndControlSubcommandsAtThirdToken() {
        TraceCommandService service = serviceWithSessions(2);

        List<String> suggestions = TraceLiveTabCompleter.complete(service, new String[] {"trace", "live", ""}, "");
        List<String> sessionIds = service.listSessionIds();

        assertEquals(2, sessionIds.size());
        assertTrue(suggestions.containsAll(sessionIds));
        assertTrue(suggestions.contains("status"));
        assertTrue(suggestions.contains("stop"));
        assertTrue(suggestions.contains("pause"));
        assertTrue(suggestions.contains("resume"));
    }

    @Test
    void filtersPartialControlSubcommand() {
        TraceCommandService service = serviceWithSessions(0);

        List<String> suggestions = TraceLiveTabCompleter.complete(service, new String[] {"trace", "live", "st"}, "st");

        assertEquals(List.of("status", "stop"), suggestions);
    }

    @Test
    void completesFlagsAfterSessionId() {
        TraceCommandService service = serviceWithSessions(1);
        String sessionId = service.listSessionIds().getFirst();

        List<String> suggestions =
                TraceLiveTabCompleter.complete(service, new String[] {"trace", "live", sessionId, ""}, "--");

        assertTrue(suggestions.contains("--filter-plugin"));
        assertTrue(suggestions.contains("--display"));
    }

    @Test
    void completesFlagsWhenUpdatingExistingSubscription() {
        TraceCommandService service = serviceWithSessions(0);

        List<String> suggestions = TraceLiveTabCompleter.complete(service, new String[] {"trace", "live", "--"}, "--");

        assertTrue(suggestions.contains("--filter-plugin"));
    }

    private static TraceCommandService serviceWithSessions(int count) {
        TraceSessionManager manager = new TraceSessionManager();
        TraceSessionConfig config = new TraceSessionConfig(
                "org.bukkit.event.player.PlayerInteractEvent",
                TraceFilter.Builder.unrestricted().build(),
                Optional.empty(),
                Optional.empty());
        long nowMillis = System.currentTimeMillis();
        for (int index = 0; index < count; index++) {
            manager.startSession(config, "admin" + index, nowMillis);
        }
        return new TraceCommandService(manager, new StubListenerRegistry(), new NoOpTraceHookPort());
    }

    private static final class StubListenerRegistry implements ListenerRegistryPort {
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
            return List.of();
        }

        @Override
        public List<String> listKnownEventClassNames() {
            return List.of();
        }
    }

    private static final class NoOpTraceHookPort implements TraceHookPort {
        @Override
        public void registerHooksForEvent(String eventClassName) {
            // Test stub: hook registration is not required for tab completion tests.
        }

        @Override
        public void syncWithActiveSessions(TraceSessionManager traceSessionManager) {
            // Test stub: hook sync is not required for tab completion tests.
        }
    }
}
