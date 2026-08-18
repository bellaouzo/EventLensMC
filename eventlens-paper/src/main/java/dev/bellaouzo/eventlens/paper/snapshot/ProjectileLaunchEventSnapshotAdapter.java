package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileLaunchEvent;

final class ProjectileLaunchEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof ProjectileLaunchEvent launchEvent)) {
            return;
        }
        collector.putString("projectile.type", launchEvent.getEntityType().name());
        if (launchEvent.getEntity().getShooter() instanceof org.bukkit.entity.Player player) {
            PlayerSnapshotFields.contribute(collector, player);
        }
    }
}
