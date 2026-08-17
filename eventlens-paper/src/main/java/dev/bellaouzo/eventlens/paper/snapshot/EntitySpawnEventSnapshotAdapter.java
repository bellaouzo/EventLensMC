package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.Location;
import org.bukkit.event.Event;

final class EntitySpawnEventSnapshotAdapter implements EventSnapshotAdapter {

    private static final String ENTITY_SPAWN_EVENT_CLASS = "org.bukkit.event.entity.EntitySpawnEvent";

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!ENTITY_SPAWN_EVENT_CLASS.equals(event.getClass().getName())) {
            return;
        }
        try {
            Object entityType = event.getClass().getMethod("getEntityType").invoke(event);
            collector.putString("spawn.entityType", String.valueOf(entityType));
            Object locationObject = event.getClass().getMethod("getLocation").invoke(event);
            if (locationObject instanceof Location location) {
                collector.putString(
                        "spawn.world",
                        location.getWorld() == null
                                ? "unknown"
                                : location.getWorld().getName());
                collector.putNumber("spawn.x", location.getBlockX());
                collector.putNumber("spawn.y", location.getBlockY());
                collector.putNumber("spawn.z", location.getBlockZ());
            } else {
                collector.putUnsupported("spawn.location", "location unavailable");
            }
        } catch (ReflectiveOperationException ex) {
            collector.putUnsupported("spawn.reflection", ex.getClass().getSimpleName());
        }
    }
}
