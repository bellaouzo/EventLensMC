package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;

public record PluginSearchResult(PluginSearchOutcome outcome, String resolvedPluginName, List<String> candidateNames) {

    public static PluginSearchResult found(String pluginName) {
        return new PluginSearchResult(PluginSearchOutcome.FOUND, pluginName, List.of());
    }

    public static PluginSearchResult ambiguous(List<String> candidateNames) {
        return new PluginSearchResult(PluginSearchOutcome.AMBIGUOUS, null, List.copyOf(candidateNames));
    }

    public static PluginSearchResult notFound() {
        return new PluginSearchResult(PluginSearchOutcome.NOT_FOUND, null, List.of());
    }
}
