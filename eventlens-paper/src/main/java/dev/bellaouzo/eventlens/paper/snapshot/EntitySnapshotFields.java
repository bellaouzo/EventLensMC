package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

final class EntitySnapshotFields {

    private EntitySnapshotFields() {}

    static void contribute(SnapshotFieldCollector collector, Entity entity) {
        collector.putString("entity.type", entity.getType().name());
        if (entity instanceof Player player) {
            PlayerSnapshotFields.contribute(collector, player);
            return;
        }
        LocationSnapshotFields.contribute(collector, "entity", entity.getLocation());
    }
}
