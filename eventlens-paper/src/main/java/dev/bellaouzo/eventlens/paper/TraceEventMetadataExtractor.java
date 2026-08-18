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
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;

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
                List.copyOf(listenerPluginNames),
                extractPlayerId(event));
    }

    private static Optional<String> extractPlayerName(Event event) {
        return extractPlayer(event).map(org.bukkit.entity.Player::getName);
    }

    private static Optional<String> extractPlayerId(Event event) {
        return extractPlayer(event).map(player -> player.getUniqueId().toString());
    }

    private static Optional<org.bukkit.entity.Player> extractPlayer(Event event) {
        if (event instanceof PlayerEvent playerEvent) {
            return Optional.of(playerEvent.getPlayer());
        }
        if (event instanceof EntityEvent entityEvent
                && entityEvent.getEntity() instanceof org.bukkit.entity.Player player) {
            return Optional.of(player);
        }
        Optional<org.bukkit.entity.Player> inventoryPlayer = extractInventoryPlayer(event);
        if (inventoryPlayer.isPresent()) {
            return inventoryPlayer;
        }
        if (event instanceof SignChangeEvent signEvent) {
            return Optional.of(signEvent.getPlayer());
        }
        if (event instanceof BlockIgniteEvent igniteEvent && igniteEvent.getPlayer() != null) {
            return Optional.of(igniteEvent.getPlayer());
        }
        if (event instanceof BlockFertilizeEvent fertilizeEvent && fertilizeEvent.getPlayer() != null) {
            return Optional.of(fertilizeEvent.getPlayer());
        }
        if (event instanceof StructureGrowEvent growEvent && growEvent.getPlayer() != null) {
            return Optional.of(growEvent.getPlayer());
        }
        return extractVehiclePlayer(event);
    }

    private static Optional<org.bukkit.entity.Player> extractInventoryPlayer(Event event) {
        if (event instanceof InventoryEvent inventoryEvent
                && inventoryEvent.getView().getPlayer() instanceof org.bukkit.entity.Player player) {
            return Optional.of(player);
        }
        return Optional.empty();
    }

    private static Optional<org.bukkit.entity.Player> extractVehiclePlayer(Event event) {
        if (event instanceof VehicleEnterEvent enterEvent
                && enterEvent.getEntered() instanceof org.bukkit.entity.Player player) {
            return Optional.of(player);
        }
        if (event instanceof VehicleExitEvent exitEvent
                && exitEvent.getExited() instanceof org.bukkit.entity.Player player) {
            return Optional.of(player);
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
        if (event instanceof VehicleEvent vehicleEvent) {
            return Optional.of(vehicleEvent.getVehicle().getLocation());
        }
        if (event instanceof WorldEvent worldEvent) {
            return Optional.of(worldEvent.getWorld().getSpawnLocation());
        }
        if (event instanceof WeatherEvent weatherEvent) {
            return Optional.of(weatherEvent.getWorld().getSpawnLocation());
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
