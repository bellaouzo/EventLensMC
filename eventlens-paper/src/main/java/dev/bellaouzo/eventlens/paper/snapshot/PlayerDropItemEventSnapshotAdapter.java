package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;

final class PlayerDropItemEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof PlayerDropItemEvent dropEvent)) {
            return;
        }
        collector.putString(
                "item.type", dropEvent.getItemDrop().getItemStack().getType().name());
        collector.putNumber(
                "item.amount", dropEvent.getItemDrop().getItemStack().getAmount());
        PlayerSnapshotFields.contribute(collector, dropEvent.getPlayer());
    }
}
