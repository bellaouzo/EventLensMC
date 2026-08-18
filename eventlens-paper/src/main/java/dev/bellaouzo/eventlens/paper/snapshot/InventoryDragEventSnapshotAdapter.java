package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryDragEvent;

final class InventoryDragEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof InventoryDragEvent dragEvent)) {
            return;
        }
        collector.putString("drag.type", dragEvent.getType().name());
        collector.putNumber("drag.slots", dragEvent.getRawSlots().size());
        collector.putString("inventory.type", dragEvent.getInventory().getType().name());
        if (dragEvent.getWhoClicked() instanceof org.bukkit.entity.Player player) {
            PlayerSnapshotFields.contribute(collector, player);
        }
    }
}
