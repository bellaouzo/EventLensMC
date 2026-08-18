package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;

final class EntityEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof EntityEvent entityEvent) {
            EntitySnapshotFields.contribute(collector, entityEvent.getEntity());
        }
    }
}
