package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;

final class ProjectileHitEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof ProjectileHitEvent hitEvent)) {
            return;
        }
        collector.putString("projectile.type", hitEvent.getEntityType().name());
        if (hitEvent.getHitBlock() != null) {
            collector.putString(
                    "hit.block.type", hitEvent.getHitBlock().getType().name());
        }
        if (hitEvent.getHitEntity() != null) {
            collector.putString(
                    "hit.entity.type", hitEvent.getHitEntity().getType().name());
        }
    }
}
