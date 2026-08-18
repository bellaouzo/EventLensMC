package dev.bellaouzo.eventlens.command.trace;

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

class TraceCommandTabCompleterTest {

    @Test
    void completesOpenSessionIdsForStop() {
        TraceCommandService service = serviceWithSessions(2);
        List<String> sessionIds = service.listOpenSessionIds();

        List<String> suggestions = TraceCommandTabCompleter.complete(service, new String[] {"trace", "stop", ""}, "");

        assertTrue(suggestions.containsAll(sessionIds));
    }

    @Test
    void completesCorrelateSubcommandAndSessionIds() {
        TraceCommandService service = serviceWithSessions(2);
        List<String> sessionIds = service.listSessionIds();

        List<String> subcommands = TraceCommandTabCompleter.complete(service, new String[] {"trace", "corr"}, "");
        assertTrue(subcommands.contains("correlate"));

        List<String> left = TraceCommandTabCompleter.complete(service, new String[] {"trace", "correlate", ""}, "");
        assertTrue(left.containsAll(sessionIds));

        List<String> right = TraceCommandTabCompleter.complete(
                service, new String[] {"trace", "correlate", sessionIds.getFirst(), ""}, "");
        assertTrue(right.containsAll(sessionIds));
    }

    private static TraceCommandService serviceWithSessions(int count) {
        TraceSessionManager manager = new TraceSessionManager();
        TraceSessionConfig config = new TraceSessionConfig(
                "org.bukkit.event.block.BlockBreakEvent",
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
