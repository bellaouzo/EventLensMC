package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.TNTPrimeEvent;

final class BlockMechanicSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof BlockPistonEvent pistonEvent) {
            collector.putString("piston.direction", pistonEvent.getDirection().name());
            collector.putBoolean("piston.sticky", pistonEvent.isSticky());
            return;
        }
        if (event instanceof BlockRedstoneEvent redstoneEvent) {
            collector.putNumber("redstone.old", redstoneEvent.getOldCurrent());
            collector.putNumber("redstone.new", redstoneEvent.getNewCurrent());
            return;
        }
        if (event instanceof BlockFromToEvent fromToEvent) {
            collector.putString("flow.to", fromToEvent.getToBlock().getType().name());
            collector.putString("flow.face", fromToEvent.getFace().name());
            return;
        }
        if (event instanceof BlockDispenseEvent dispenseEvent) {
            collector.putString(
                    "dispense.item", dispenseEvent.getItem().getType().name());
            collector.putNumber("dispense.amount", dispenseEvent.getItem().getAmount());
            return;
        }
        if (event instanceof BlockFertilizeEvent fertilizeEvent) {
            if (fertilizeEvent.getPlayer() != null) {
                PlayerSnapshotFields.contribute(collector, fertilizeEvent.getPlayer());
            }
            collector.putNumber("fertilize.blocks", fertilizeEvent.getBlocks().size());
            return;
        }
        if (event instanceof TNTPrimeEvent primeEvent) {
            collector.putString("tnt.cause", primeEvent.getCause().name());
            return;
        }
        if (event instanceof NotePlayEvent noteEvent) {
            collector.putString("note.instrument", noteEvent.getInstrument().name());
            collector.putString("note", noteEvent.getNote().toString());
            return;
        }
        if (event instanceof CauldronLevelChangeEvent cauldronEvent) {
            collector.putString("cauldron.reason", cauldronEvent.getReason().name());
            collector.putString(
                    "cauldron.new", cauldronEvent.getNewState().getType().name());
        }
    }
}
