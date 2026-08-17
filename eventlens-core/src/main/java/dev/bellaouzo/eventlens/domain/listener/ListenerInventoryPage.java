package dev.bellaouzo.eventlens.domain.listener;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record ListenerInventoryPage(
        @NonNull String eventClassName,
        @NonNull List<ListenerRegistration> listeners,
        int page,
        int totalPages,
        int totalListeners) {}
