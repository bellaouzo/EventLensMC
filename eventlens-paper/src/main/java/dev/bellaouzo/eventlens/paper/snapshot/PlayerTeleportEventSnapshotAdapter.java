package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;

final class PlayerTeleportEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof PlayerTeleportEvent teleportEvent)) {
            return;
        }

        collector.putString("teleport.cause", teleportEvent.getCause().name());
        LocationSnapshotFields.contribute(collector, "teleport.from", teleportEvent.getFrom());
        LocationSnapshotFields.contribute(collector, "teleport.to", teleportEvent.getTo());
    }
}
