package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

final class PlayerEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof PlayerEvent playerEvent)) {
            return;
        }

        var player = playerEvent.getPlayer();
        PlayerSnapshotFields.contribute(collector, player);
    }
}
