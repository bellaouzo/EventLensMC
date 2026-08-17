package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record PluginListenerPage(
        @NonNull String pluginName,
        String filteredEventClassName,
        @NonNull List<PluginListenerBinding> bindings,
        int page,
        int totalPages,
        int totalListeners) {}
