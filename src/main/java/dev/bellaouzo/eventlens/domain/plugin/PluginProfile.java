package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record PluginProfile(
        @NonNull PluginDescriptor descriptor,
        @NonNull PluginListenerInventory inventory,
        @NonNull List<PluginCoInteraction> coInteractions,
        @NonNull PluginTraceStatistics traceStatistics) {}
