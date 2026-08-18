package dev.bellaouzo.eventlens.command.trace;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
import org.junit.jupiter.api.Test;

class TraceStartTabCompleterTest {

    @Test
    void completesSecondEventAfterComma() {
        List<String> suggestions = TraceStartTabCompleter.completeEventQuery(
                List.of("PlayerInteractEvent", "BlockBreakEvent", "BlockPlaceEvent"), "PlayerInteractEvent,");

        assertTrue(suggestions.contains("PlayerInteractEvent,BlockBreakEvent"));
        assertTrue(suggestions.contains("PlayerInteractEvent,BlockPlaceEvent"));
        assertTrue(suggestions.stream().noneMatch(value -> value.equals("PlayerInteractEvent,PlayerInteractEvent")));
    }

    @Test
    void filtersFragmentAfterComma() {
        List<String> suggestions = TraceStartTabCompleter.completeEventQuery(
                List.of("PlayerInteractEvent", "BlockBreakEvent", "BlockPlaceEvent"), "PlayerInteractEvent,BlockB");

        assertTrue(suggestions.contains("PlayerInteractEvent,BlockBreakEvent"));
        assertTrue(suggestions.stream().noneMatch(value -> value.contains("BlockPlaceEvent")));
    }

    @Test
    void completesEventFromContainedWord() {
        List<String> suggestions = TraceStartTabCompleter.completeEventQuery(
                List.of("PlayerInteractEvent", "EntityExplodeEvent", "BlockExplodeEvent"), "explode");

        assertTrue(suggestions.contains("EntityExplodeEvent"));
        assertTrue(suggestions.contains("BlockExplodeEvent"));
        assertTrue(suggestions.stream().noneMatch(value -> value.contains("PlayerInteractEvent")));
    }

    @Test
    void completesFlagsAfterConfirmHot() {
        TraceCommandService service =
                new TraceCommandService(new TraceSessionManager(), new EmptyListeners(), new NoHooks());
        List<String> suggestions = TraceCommandTabCompleter.complete(
                service, new String[] {"trace", "start", "PlayerMoveEvent", "--confirm-hot", ""}, "");

        assertTrue(suggestions.contains("--max-events"));
        assertTrue(suggestions.contains("--player"));
        assertTrue(suggestions.contains("--plugin"));
    }

    private static final class EmptyListeners implements ListenerRegistryPort {
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

    private static final class NoHooks implements TraceHookPort {
        @Override
        public void registerHooksForEvent(String eventClassName) {
            // Unused in tab-completion tests.
        }

        @Override
        public void syncWithActiveSessions(TraceSessionManager traceSessionManager) {
            // Unused in tab-completion tests.
        }
    }
}
