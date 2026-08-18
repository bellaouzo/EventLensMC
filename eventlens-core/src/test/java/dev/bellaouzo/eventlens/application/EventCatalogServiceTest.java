package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.port.EventSnapshotRegistryPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventCatalogServiceTest {

    @Test
    void listsEventsMatchingContainedWord() {
        EventCatalogService catalog = new EventCatalogService(new StubListeners(), new AllowAllSnapshots());

        List<String> names = catalog.list("explode").stream()
                .map(EventCatalogService.EventCatalogEntry::simpleName)
                .toList();

        assertTrue(names.contains("EntityExplodeEvent"));
        assertTrue(names.contains("BlockExplodeEvent"));
        assertEquals(2, names.size());
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
            return List.of("BlockBreakEvent", "EntityExplodeEvent", "BlockExplodeEvent");
        }

        @Override
        public List<String> listKnownEventClassNames() {
            return List.of(
                    "org.bukkit.event.block.BlockBreakEvent",
                    "org.bukkit.event.entity.EntityExplodeEvent",
                    "org.bukkit.event.block.BlockExplodeEvent");
        }
    }

    private static final class AllowAllSnapshots implements EventSnapshotRegistryPort {
        @Override
        public boolean supportsTrace(String eventClassName) {
            return true;
        }

        @Override
        public List<String> supportedTraceEventSimpleNames() {
            return List.of();
        }
    }
}
