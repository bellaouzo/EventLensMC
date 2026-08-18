package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;

final class CreatureSpawnEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof CreatureSpawnEvent spawnEvent)) {
            return;
        }
        collector.putString("spawn.entityType", spawnEvent.getEntityType().name());
        collector.putString("spawn.reason", spawnEvent.getSpawnReason().name());
    }
}
