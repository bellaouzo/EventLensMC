package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

final class PlayerActionSnapshotAdapter implements EventSnapshotAdapter {

    private static final String HAND = "interact.hand";

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof PlayerInteractEntityEvent interactEvent) {
            contributeInteract(collector, interactEvent);
            return;
        }
        if (event instanceof PlayerItemConsumeEvent consumeEvent) {
            putItem(collector, "item", consumeEvent.getItem());
            collector.putString(HAND, consumeEvent.getHand().name());
            return;
        }
        if (event instanceof PlayerSwapHandItemsEvent swapEvent) {
            putItem(collector, "main", swapEvent.getMainHandItem());
            putItem(collector, "off", swapEvent.getOffHandItem());
            return;
        }
        if (event instanceof PlayerBucketEvent bucketEvent) {
            collector.putString("bucket.type", bucketEvent.getBucket().name());
            collector.putString("bucket.hand", bucketEvent.getHand().name());
            collector.putString(
                    "clicked.block.type",
                    bucketEvent.getBlockClicked().getType().name());
            collector.putString("clicked.face", bucketEvent.getBlockFace().name());
            return;
        }
        if (event instanceof PlayerFishEvent fishEvent) {
            contributeFish(collector, fishEvent);
            return;
        }
        if (event instanceof CraftItemEvent craftEvent) {
            putItem(collector, "craft.result", craftEvent.getRecipe().getResult());
        }
    }

    private static void contributeInteract(SnapshotFieldCollector collector, PlayerInteractEntityEvent interactEvent) {
        collector.putString(
                "clicked.entity.type", interactEvent.getRightClicked().getType().name());
        collector.putString(HAND, interactEvent.getHand().name());
        if (interactEvent instanceof PlayerInteractAtEntityEvent atEvent) {
            collector.putNumber("clicked.offset.x", atEvent.getClickedPosition().getX());
            collector.putNumber("clicked.offset.y", atEvent.getClickedPosition().getY());
            collector.putNumber("clicked.offset.z", atEvent.getClickedPosition().getZ());
        }
    }

    private static void contributeFish(SnapshotFieldCollector collector, PlayerFishEvent fishEvent) {
        collector.putString("fish.state", fishEvent.getState().name());
        collector.putNumber("fish.exp", fishEvent.getExpToDrop());
        if (fishEvent.getHand() != null) {
            collector.putString(HAND, fishEvent.getHand().name());
        }
        if (fishEvent.getCaught() != null) {
            collector.putString(
                    "fish.caught.type", fishEvent.getCaught().getType().name());
        }
    }

    private static void putItem(SnapshotFieldCollector collector, String prefix, ItemStack item) {
        if (item == null) {
            collector.putUnsupported(prefix + ".type", "no item");
            return;
        }
        collector.putString(prefix + ".type", item.getType().name());
        collector.putNumber(prefix + ".amount", item.getAmount());
    }
}
