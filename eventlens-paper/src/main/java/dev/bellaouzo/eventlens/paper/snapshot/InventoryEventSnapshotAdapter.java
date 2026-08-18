package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryEvent;

final class InventoryEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof InventoryEvent inventoryEvent)) {
            return;
        }
        collector.putString(
                "inventory.type", inventoryEvent.getInventory().getType().name());
        if (inventoryEvent.getView().getPlayer() instanceof Player player) {
            PlayerSnapshotFields.contribute(collector, player);
        }
    }
}
