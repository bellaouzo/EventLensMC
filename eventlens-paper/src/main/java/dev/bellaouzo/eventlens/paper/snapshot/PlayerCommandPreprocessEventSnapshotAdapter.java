package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

final class PlayerCommandPreprocessEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof PlayerCommandPreprocessEvent commandEvent)) {
            return;
        }

        collector.putString("command.message", commandEvent.getMessage());
    }
}
