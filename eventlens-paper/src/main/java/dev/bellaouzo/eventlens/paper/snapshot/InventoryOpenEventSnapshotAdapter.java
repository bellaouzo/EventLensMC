package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryOpenEvent;

final class InventoryOpenEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof InventoryOpenEvent openEvent)) {
            return;
        }
        collector.putString("inventory.type", openEvent.getInventory().getType().name());
        if (openEvent.getPlayer() instanceof org.bukkit.entity.Player player) {
            PlayerSnapshotFields.contribute(collector, player);
        }
    }
}
