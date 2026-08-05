package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;

final class BlockEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof BlockEvent blockEvent)) {
            return;
        }

        var block = blockEvent.getBlock();
        collector.putString("block.type", block.getType().name());
        collector.putString("block.world", block.getWorld().getName());
        collector.putNumber("block.x", block.getX());
        collector.putNumber("block.y", block.getY());
        collector.putNumber("block.z", block.getZ());

        if (event instanceof BlockBreakEvent blockBreak) {
            PlayerSnapshotFields.contribute(collector, blockBreak.getPlayer());
        } else if (event instanceof BlockPlaceEvent blockPlace) {
            PlayerSnapshotFields.contribute(collector, blockPlace.getPlayer());
        }
    }
}
