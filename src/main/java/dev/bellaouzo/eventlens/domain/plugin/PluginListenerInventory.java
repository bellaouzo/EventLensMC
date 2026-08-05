package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

public record PluginListenerInventory(
        @NonNull String pluginName,
        @NonNull List<PluginListenerBinding> bindings,
        @NonNull List<String> eventClassNames,
        @NonNull Map<String, Integer> listenerCountByEvent,
        @NonNull Map<String, Integer> listenerCountByPriority,
        @NonNull Map<String, Integer> registryCoInteractions) {}
