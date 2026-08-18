package dev.bellaouzo.eventlens.command.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.EventCatalogService;
import dev.bellaouzo.eventlens.application.port.EventSnapshotRegistryPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.command.RecordingCommandSender;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventsCommandHandlerTest {

    private final EventsCommandHandler handler =
            new EventsCommandHandler(new EventCatalogService(new StubListeners(), new StubSnapshots()));

    @Test
    void listsTraceableEvents() {
        RecordingCommandSender sender = new RecordingCommandSender(true);
        handler.handle(sender.sender(), new String[] {"events"});
        assertTrue(sender.joined().contains("Registered events"));
        assertTrue(sender.joined().contains("BlockBreakEvent"));
    }

    @Test
    void filtersByPrefix() {
        RecordingCommandSender sender = new RecordingCommandSender(true);
        handler.handle(sender.sender(), new String[] {"events", "Interact"});
        assertTrue(sender.joined().contains("PlayerInteractEvent"));
        assertTrue(!sender.joined().contains("BlockBreakEvent"));
    }

    @Test
    void deniesWithoutPermission() {
        RecordingCommandSender sender = new RecordingCommandSender(false);
        handler.handle(sender.sender(), new String[] {"events"});
        assertTrue(sender.joined().contains("You do not have permission."));
    }

    @Test
    void tabCompletesEventNames() {
        assertEquals(List.of("BlockBreakEvent"), handler.tabComplete("Break"));
    }

    private static final class StubListeners implements ListenerRegistryPort {
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
            return List.of("BlockBreakEvent", "PlayerInteractEvent");
        }

        @Override
        public List<String> listKnownEventClassNames() {
            return List.of("org.bukkit.event.block.BlockBreakEvent", "org.bukkit.event.player.PlayerInteractEvent");
        }
    }

    private static final class StubSnapshots implements EventSnapshotRegistryPort {
        @Override
        public boolean supportsTrace(String eventClassName) {
            return true;
        }

        @Override
        public List<String> supportedTraceEventSimpleNames() {
            return List.of("BlockBreakEvent", "PlayerInteractEvent");
        }
    }
}
