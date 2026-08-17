package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record PluginCompareResult(
        @NonNull PluginProfile left,
        @NonNull PluginProfile right,
        @NonNull List<String> sharedEvents,
        @NonNull List<String> leftOnlyEvents,
        @NonNull List<String> rightOnlyEvents,
        @NonNull List<String> sharedCoPlugins) {}
