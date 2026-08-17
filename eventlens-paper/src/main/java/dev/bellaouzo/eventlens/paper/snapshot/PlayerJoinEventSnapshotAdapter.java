package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;

final class PlayerJoinEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof PlayerJoinEvent joinEvent)) {
            return;
        }
        collector.putString("join.player", joinEvent.getPlayer().getName());
        collector.putString("join.world", joinEvent.getPlayer().getWorld().getName());
        if (joinEvent.joinMessage() != null) {
            collector.putString("join.message", joinEvent.joinMessage().toString());
        }
    }
}
