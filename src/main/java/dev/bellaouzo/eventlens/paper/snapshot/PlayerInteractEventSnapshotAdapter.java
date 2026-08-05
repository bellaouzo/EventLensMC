package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

final class PlayerInteractEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof PlayerInteractEvent interactEvent)) {
            return;
        }

        collector.putString("interact.action", interactEvent.getAction().name());
        if (interactEvent.getHand() != null) {
            collector.putString("interact.hand", interactEvent.getHand().name());
        }
        if (interactEvent.getClickedBlock() != null) {
            collector.putString(
                    "clicked.block.type",
                    interactEvent.getClickedBlock().getType().name());
        }
        if (interactEvent.getItem() != null) {
            collector.putString("item.type", interactEvent.getItem().getType().name());
        }
    }
}
