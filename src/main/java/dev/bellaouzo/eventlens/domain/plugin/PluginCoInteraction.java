package dev.bellaouzo.eventlens.domain.plugin;

import org.jspecify.annotations.NonNull;

public record PluginCoInteraction(@NonNull String pluginName, int sharedEventCount, int traceCoDispatchCount) {

    public int totalScore() {
        return sharedEventCount + traceCoDispatchCount;
    }
}
