package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

final class PlayerMoveEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof PlayerMoveEvent moveEvent)) {
            return;
        }

        LocationSnapshotFields.contribute(collector, "move.from", moveEvent.getFrom());
        LocationSnapshotFields.contribute(collector, "move.to", moveEvent.getTo());
    }
}
