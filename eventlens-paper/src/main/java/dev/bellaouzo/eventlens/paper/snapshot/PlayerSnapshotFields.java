package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.Location;
import org.bukkit.entity.Player;

final class PlayerSnapshotFields {

    private PlayerSnapshotFields() {}

    static void contribute(SnapshotFieldCollector collector, Player player) {
        collector.putString("player.name", player.getName());
        collector.putString("player.uuid", player.getUniqueId().toString());
        collector.putBoolean("player.op", player.isOp());
        collector.putBoolean("player.online", player.isOnline());

        Location location = player.getLocation();
        if (location.getWorld() != null) {
            collector.putString("player.world", location.getWorld().getName());
        }
        collector.putNumber("player.x", location.getBlockX());
        collector.putNumber("player.y", location.getBlockY());
        collector.putNumber("player.z", location.getBlockZ());
    }
}
