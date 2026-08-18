package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;

final class InventoryCloseEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof InventoryCloseEvent closeEvent)) {
            return;
        }
        collector.putString(
                "inventory.type", closeEvent.getInventory().getType().name());
        if (closeEvent.getPlayer() instanceof org.bukkit.entity.Player player) {
            PlayerSnapshotFields.contribute(collector, player);
        }
    }
}
