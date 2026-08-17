package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;

final class InventoryClickEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof InventoryClickEvent clickEvent)) {
            return;
        }

        collector.putString("click.type", clickEvent.getClick().name());
        collector.putNumber("click.slot", clickEvent.getSlot());
        collector.putNumber("click.rawSlot", clickEvent.getRawSlot());
        collector.putString(
                "inventory.type", clickEvent.getInventory().getType().name());
        if (clickEvent.getCurrentItem() != null) {
            collector.putString(
                    "item.type", clickEvent.getCurrentItem().getType().name());
        }
        if (clickEvent.getWhoClicked() instanceof org.bukkit.entity.Player player) {
            PlayerSnapshotFields.contribute(collector, player);
        }
    }
}
