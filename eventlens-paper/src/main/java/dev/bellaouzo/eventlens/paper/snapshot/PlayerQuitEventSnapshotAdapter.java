package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;

final class PlayerQuitEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof PlayerQuitEvent quitEvent)) {
            return;
        }
        collector.putString("quit.player", quitEvent.getPlayer().getName());
        collector.putString("quit.world", quitEvent.getPlayer().getWorld().getName());
        if (quitEvent.quitMessage() != null) {
            collector.putString("quit.message", quitEvent.quitMessage().toString());
        }
    }
}
