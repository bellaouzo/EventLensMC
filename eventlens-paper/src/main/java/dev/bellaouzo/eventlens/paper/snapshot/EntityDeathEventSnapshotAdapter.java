package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;

final class EntityDeathEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof EntityDeathEvent deathEvent)) {
            return;
        }

        collector.putString("entity.type", deathEvent.getEntity().getType().name());
        collector.putNumber("death.droppedExp", deathEvent.getDroppedExp());
        if (deathEvent.getEntity().getKiller() != null) {
            collector.putString(
                    "death.killer", deathEvent.getEntity().getKiller().getName());
        }
    }
}
