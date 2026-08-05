package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.Location;

final class LocationSnapshotFields {

    private LocationSnapshotFields() {}

    static void contribute(SnapshotFieldCollector collector, String prefix, Location location) {
        if (location.getWorld() != null) {
            collector.putString(prefix + ".world", location.getWorld().getName());
        }
        collector.putNumber(prefix + ".x", location.getBlockX());
        collector.putNumber(prefix + ".y", location.getBlockY());
        collector.putNumber(prefix + ".z", location.getBlockZ());
    }
}
