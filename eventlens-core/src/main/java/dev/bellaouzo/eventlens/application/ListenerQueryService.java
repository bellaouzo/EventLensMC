package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.conflict.DispatchConflict;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerInventoryPage;
import dev.bellaouzo.eventlens.domain.listener.ListenerInventoryResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import java.util.List;

public final class ListenerQueryService {

    public static final int DEFAULT_PAGE_SIZE = 6;

    private final ListenerRegistryPort listenerRegistryPort;

    public ListenerQueryService(ListenerRegistryPort listenerRegistryPort) {
        this.listenerRegistryPort = listenerRegistryPort;
    }

    public ListenerInventoryResult queryListeners(String eventQuery, int page) {
        return queryListeners(eventQuery, page, DEFAULT_PAGE_SIZE);
    }

    public ListenerInventoryResult queryListeners(String eventQuery, int page, int pageSize) {
        if (eventQuery == null || eventQuery.isBlank()) {
            return new ListenerInventoryResult.NotFound("");
        }

        int safePageSize = Math.max(1, pageSize);
        EventSearchResult search = listenerRegistryPort.searchEvents(eventQuery.trim());

        return switch (search.outcome()) {
            case NOT_FOUND -> new ListenerInventoryResult.NotFound(eventQuery.trim());
            case AMBIGUOUS -> new ListenerInventoryResult.Ambiguous(search.candidateClassNames());
            case FOUND -> paginate(search.resolvedEventClassName(), page, safePageSize);
        };
    }

    public List<String> listKnownEventSimpleNames() {
        return listenerRegistryPort.listKnownEventSimpleNames();
    }

    public List<DispatchConflict> inventoryConflicts(String eventClassName) {
        return SessionConflictAnalyzer.analyzeInventory(listenerRegistryPort.getListeners(eventClassName));
    }

    private ListenerInventoryResult paginate(String eventClassName, int page, int pageSize) {
        List<ListenerRegistration> listeners = listenerRegistryPort.getListeners(eventClassName);
        int totalListeners = listeners.size();
        int totalPages = totalListeners == 0 ? 1 : (int) Math.ceil((double) totalListeners / pageSize);

        if (page < 1 || page > totalPages) {
            return new ListenerInventoryResult.InvalidPage(page, totalPages);
        }

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalListeners);
        List<ListenerRegistration> pageItems = listeners.subList(fromIndex, toIndex);

        return new ListenerInventoryResult.Success(
                new ListenerInventoryPage(eventClassName, List.copyOf(pageItems), page, totalPages, totalListeners));
    }
}
