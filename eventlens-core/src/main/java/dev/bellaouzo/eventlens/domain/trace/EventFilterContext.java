package dev.bellaouzo.eventlens.domain.trace;

import java.util.List;
import java.util.Optional;

public record EventFilterContext(
        String eventClassName,
        boolean cancellable,
        boolean cancelled,
        Optional<String> playerName,
        Optional<String> worldName,
        Optional<Integer> blockX,
        Optional<Integer> blockY,
        Optional<Integer> blockZ,
        List<String> listenerPluginNames,
        Optional<String> playerId) {

    public EventFilterContext(
            String eventClassName,
            boolean cancellable,
            boolean cancelled,
            Optional<String> playerName,
            Optional<String> worldName,
            Optional<Integer> blockX,
            Optional<Integer> blockY,
            Optional<Integer> blockZ,
            List<String> listenerPluginNames) {
        this(
                eventClassName,
                cancellable,
                cancelled,
                playerName,
                worldName,
                blockX,
                blockY,
                blockZ,
                listenerPluginNames,
                Optional.empty());
    }
}
