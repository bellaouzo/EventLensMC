package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.EventSnapshotRegistryPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.observability.SamplingPolicy;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class EventCatalogService {

    public record EventCatalogEntry(String simpleName, String className, String coverage) {}

    private final ListenerRegistryPort listenerRegistryPort;
    private final EventSnapshotRegistryPort snapshotRegistryPort;

    public EventCatalogService(
            ListenerRegistryPort listenerRegistryPort, EventSnapshotRegistryPort snapshotRegistryPort) {
        this.listenerRegistryPort = listenerRegistryPort;
        this.snapshotRegistryPort = snapshotRegistryPort;
    }

    public List<EventCatalogEntry> list(String prefix) {
        String normalized = prefix == null ? "" : prefix.trim().toLowerCase(Locale.ROOT);
        List<EventCatalogEntry> entries = new ArrayList<>();
        for (String className : listenerRegistryPort.listKnownEventClassNames()) {
            String simple = SupportedEventTypes.displaySimpleName(className);
            if (!normalized.isEmpty() && !simple.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                continue;
            }
            entries.add(new EventCatalogEntry(simple, className, coverage(className)));
        }
        entries.sort(Comparator.comparing(EventCatalogEntry::simpleName, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(entries);
    }

    private String coverage(String className) {
        if (SamplingPolicy.requiresNarrowingFilter(className)) {
            return "hot";
        }
        if (snapshotRegistryPort.supportsTrace(className)) {
            return "traceable";
        }
        return "generic-only";
    }
}
