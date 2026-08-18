package dev.bellaouzo.eventlens.application;

import java.util.List;
import java.util.Optional;

public record TracePreset(
        String name,
        Optional<String> pluginName,
        Optional<String> playerName,
        Optional<String> worldName,
        Optional<Long> maxDurationMillis,
        Optional<Integer> maxEventCount,
        Optional<Long> slowThresholdNanos,
        boolean captureStacks,
        List<String> extraFlags,
        List<String> eventSimpleNames) {

    public TracePreset {
        extraFlags = extraFlags == null ? List.of() : List.copyOf(extraFlags);
        eventSimpleNames = eventSimpleNames == null ? List.of() : List.copyOf(eventSimpleNames);
    }
}
