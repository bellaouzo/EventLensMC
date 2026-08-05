package dev.bellaouzo.eventlens.domain.observability;

import org.jspecify.annotations.NonNull;

public record RankedPluginTiming(@NonNull String pluginName, DurationStats stats, int invocationCount) {}
