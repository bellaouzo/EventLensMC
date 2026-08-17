package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

final class EntityDamageEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof EntityDamageEvent damageEvent)) {
            return;
        }

        collector.putNumber("damage.amount", damageEvent.getDamage());
        collector.putNumber("damage.final", damageEvent.getFinalDamage());
        collector.putString("damage.cause", damageEvent.getCause().name());
        collector.putString("entity.type", damageEvent.getEntity().getType().name());
    }
}
