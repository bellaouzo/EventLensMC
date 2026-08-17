package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.domain.trace.EventFilterContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;

final class TraceEventMetadataExtractor {

    private TraceEventMetadataExtractor() {}

    static EventFilterContext extract(Event event, List<String> listenerPluginNames) {
        Optional<String> playerName = extractPlayerName(event);
        Optional<String> worldName = Optional.empty();
        Optional<Integer> blockX = Optional.empty();
        Optional<Integer> blockY = Optional.empty();
        Optional<Integer> blockZ = Optional.empty();

        Optional<Location> location = extractLocation(event);
        if (location.isPresent()) {
            Location resolved = location.get();
            if (resolved.getWorld() != null) {
                worldName = Optional.of(resolved.getWorld().getName());
            }
            blockX = Optional.of(resolved.getBlockX());
            blockY = Optional.of(resolved.getBlockY());
            blockZ = Optional.of(resolved.getBlockZ());
        }

        boolean cancellable = event instanceof Cancellable;
        boolean cancelled = cancellable && ((Cancellable) event).isCancelled();

        return new EventFilterContext(
                event.getClass().getName(),
                cancellable,
                cancelled,
                playerName,
                worldName,
                blockX,
                blockY,
                blockZ,
                List.copyOf(listenerPluginNames));
    }

    private static Optional<String> extractPlayerName(Event event) {
        if (event instanceof PlayerEvent playerEvent) {
            return Optional.of(playerEvent.getPlayer().getName());
        }
        if (event instanceof EntityEvent entityEvent
                && entityEvent.getEntity() instanceof org.bukkit.entity.Player player) {
            return Optional.of(player.getName());
        }
        return Optional.empty();
    }

    private static Optional<Location> extractLocation(Event event) {
        if (event instanceof PlayerEvent playerEvent) {
            return Optional.of(playerEvent.getPlayer().getLocation());
        }
        if (event instanceof BlockEvent blockEvent) {
            return Optional.of(blockEvent.getBlock().getLocation());
        }
        if (event instanceof EntityEvent entityEvent) {
            return Optional.of(entityEvent.getEntity().getLocation());
        }
        return Optional.empty();
    }

    static List<String> normalizePluginNames(List<String> pluginNames) {
        List<String> normalized = new ArrayList<>(pluginNames.size());
        for (String pluginName : pluginNames) {
            normalized.add(pluginName.toLowerCase(Locale.ROOT));
        }
        return normalized;
    }
}
