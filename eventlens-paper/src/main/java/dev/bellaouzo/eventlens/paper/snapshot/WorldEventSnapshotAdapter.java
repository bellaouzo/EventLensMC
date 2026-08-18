package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.WorldEvent;

final class WorldEventSnapshotAdapter implements EventSnapshotAdapter {

    private static final String WORLD = "world";

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof ChunkEvent chunkEvent) {
            collector.putString(WORLD, chunkEvent.getWorld().getName());
            collector.putNumber("chunk.x", chunkEvent.getChunk().getX());
            collector.putNumber("chunk.z", chunkEvent.getChunk().getZ());
            return;
        }
        if (event instanceof WorldEvent worldEvent) {
            collector.putString(WORLD, worldEvent.getWorld().getName());
            return;
        }
        if (event instanceof WeatherEvent weatherEvent) {
            collector.putString(WORLD, weatherEvent.getWorld().getName());
        }
    }
}
