package dev.bellaouzo.eventlens.paper.snapshot;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

final class ProtectionAndMobSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof SignChangeEvent signEvent) {
            contributeSign(collector, signEvent);
            return;
        }
        if (event instanceof EntityChangeBlockEvent changeEvent) {
            contributeChangeBlock(collector, changeEvent);
            return;
        }
        if (event instanceof BlockIgniteEvent igniteEvent) {
            contributeIgnite(collector, igniteEvent);
            return;
        }
        if (event instanceof BlockBurnEvent burnEvent) {
            contributeBurn(collector, burnEvent);
            return;
        }
        if (event instanceof VehicleEnterEvent enterEvent) {
            collector.putString(
                    "vehicle.type", enterEvent.getVehicle().getType().name());
            EntitySnapshotFields.contribute(collector, enterEvent.getEntered());
            return;
        }
        if (event instanceof VehicleExitEvent exitEvent) {
            collector.putString("vehicle.type", exitEvent.getVehicle().getType().name());
            EntitySnapshotFields.contribute(collector, exitEvent.getExited());
            return;
        }
        if (event instanceof EntityTargetEvent targetEvent) {
            contributeTarget(collector, targetEvent);
            return;
        }
        if (event instanceof FoodLevelChangeEvent foodEvent) {
            contributeFood(collector, foodEvent);
        }
    }

    private static void contributeSign(SnapshotFieldCollector collector, SignChangeEvent signEvent) {
        PlayerSnapshotFields.contribute(collector, signEvent.getPlayer());
        collector.putString("sign.side", signEvent.getSide().name());
        var lines = signEvent.lines();
        collector.putNumber("sign.lines", lines.size());
        if (!lines.isEmpty()) {
            collector.putString(
                    "sign.line0", PlainTextComponentSerializer.plainText().serialize(lines.getFirst()));
        }
    }

    private static void contributeChangeBlock(SnapshotFieldCollector collector, EntityChangeBlockEvent changeEvent) {
        EntitySnapshotFields.contribute(collector, changeEvent.getEntity());
        collector.putString("block.type", changeEvent.getBlock().getType().name());
        collector.putString("block.to", changeEvent.getTo().name());
        LocationSnapshotFields.contribute(
                collector, "block", changeEvent.getBlock().getLocation());
    }

    private static void contributeIgnite(SnapshotFieldCollector collector, BlockIgniteEvent igniteEvent) {
        collector.putString("ignite.cause", igniteEvent.getCause().name());
        if (igniteEvent.getPlayer() != null) {
            PlayerSnapshotFields.contribute(collector, igniteEvent.getPlayer());
        }
        Entity igniter = igniteEvent.getIgnitingEntity();
        if (igniter != null) {
            collector.putString("ignite.entity.type", igniter.getType().name());
        }
    }

    private static void contributeBurn(SnapshotFieldCollector collector, BlockBurnEvent burnEvent) {
        Block igniting = burnEvent.getIgnitingBlock();
        if (igniting != null) {
            collector.putString("burn.source.type", igniting.getType().name());
        }
    }

    private static void contributeTarget(SnapshotFieldCollector collector, EntityTargetEvent targetEvent) {
        EntitySnapshotFields.contribute(collector, targetEvent.getEntity());
        collector.putString("target.reason", targetEvent.getReason().name());
        if (targetEvent.getTarget() != null) {
            collector.putString("target.type", targetEvent.getTarget().getType().name());
            return;
        }
        collector.putUnsupported("target.type", "no target");
    }

    private static void contributeFood(SnapshotFieldCollector collector, FoodLevelChangeEvent foodEvent) {
        EntitySnapshotFields.contribute(collector, foodEvent.getEntity());
        collector.putNumber("food.level", foodEvent.getFoodLevel());
        if (foodEvent.getItem() != null) {
            collector.putString("food.item.type", foodEvent.getItem().getType().name());
        }
    }
}
