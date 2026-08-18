package dev.bellaouzo.eventlens.paper.snapshot;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

final class PlayerWorldSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof PlayerRespawnEvent respawnEvent) {
            LocationSnapshotFields.contribute(collector, "respawn", respawnEvent.getRespawnLocation());
            collector.putBoolean("respawn.bed", respawnEvent.isBedSpawn());
            collector.putBoolean("respawn.anchor", respawnEvent.isAnchorSpawn());
            collector.putString(
                    "respawn.reason", respawnEvent.getRespawnReason().name());
            return;
        }
        if (event instanceof PlayerChangedWorldEvent changedWorldEvent) {
            collector.putString("world.from", changedWorldEvent.getFrom().getName());
            return;
        }
        if (event instanceof PlayerPortalEvent portalEvent) {
            collector.putBoolean("portal.canCreate", portalEvent.getCanCreatePortal());
            collector.putNumber("portal.searchRadius", portalEvent.getSearchRadius());
            return;
        }
        if (event instanceof PlayerKickEvent kickEvent) {
            collector.putString("kick.cause", kickEvent.getCause().name());
            collector.putString(
                    "kick.reason", PlainTextComponentSerializer.plainText().serialize(kickEvent.reason()));
        }
    }
}
