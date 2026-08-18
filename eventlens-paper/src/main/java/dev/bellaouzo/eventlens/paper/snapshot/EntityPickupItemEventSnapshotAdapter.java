package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;

final class EntityPickupItemEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof EntityPickupItemEvent pickupEvent)) {
            return;
        }
        collector.putString(
                "item.type", pickupEvent.getItem().getItemStack().getType().name());
        collector.putNumber("item.amount", pickupEvent.getItem().getItemStack().getAmount());
        collector.putString("entity.type", pickupEvent.getEntityType().name());
        if (pickupEvent.getEntity() instanceof org.bukkit.entity.Player player) {
            PlayerSnapshotFields.contribute(collector, player);
        }
    }
}
