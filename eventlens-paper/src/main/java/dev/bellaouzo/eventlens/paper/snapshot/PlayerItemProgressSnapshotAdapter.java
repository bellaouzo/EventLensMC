package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

final class PlayerItemProgressSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof PlayerItemDamageEvent damageEvent) {
            putItem(collector, "item", damageEvent.getItem());
            collector.putNumber("item.damage", damageEvent.getDamage());
            return;
        }
        if (event instanceof PlayerItemBreakEvent breakEvent) {
            putItem(collector, "item", breakEvent.getBrokenItem());
            return;
        }
        if (event instanceof PlayerItemMendEvent mendEvent) {
            putItem(collector, "item", mendEvent.getItem());
            collector.putNumber("mend.repair", mendEvent.getRepairAmount());
            collector.putNumber("mend.xp", mendEvent.getConsumedExperience());
            return;
        }
        if (event instanceof PlayerAttemptPickupItemEvent pickupEvent) {
            putItem(collector, "item", pickupEvent.getItem().getItemStack());
            collector.putNumber("item.remaining", pickupEvent.getRemaining());
            return;
        }
        if (event instanceof PlayerArmorStandManipulateEvent armorEvent) {
            collector.putString("slot", armorEvent.getSlot().name());
            putItem(collector, "player.item", armorEvent.getPlayerItem());
            putItem(collector, "stand.item", armorEvent.getArmorStandItem());
            return;
        }
        if (event instanceof PlayerShearEntityEvent shearEvent) {
            collector.putString("sheared.type", shearEvent.getEntity().getType().name());
            return;
        }
        if (event instanceof PlayerLeashEntityEvent leashEvent) {
            collector.putString("leash.type", leashEvent.getEntity().getType().name());
            return;
        }
        if (event instanceof PlayerHarvestBlockEvent harvestEvent) {
            collector.putString(
                    "harvest.block", harvestEvent.getHarvestedBlock().getType().name());
            return;
        }
        if (event instanceof PlayerRecipeDiscoverEvent recipeEvent) {
            collector.putString("recipe", recipeEvent.getRecipe().toString());
            return;
        }
        if (event instanceof PlayerEditBookEvent bookEvent) {
            collector.putBoolean("book.signing", bookEvent.isSigning());
            return;
        }
        if (event instanceof PlayerEggThrowEvent eggEvent) {
            collector.putBoolean("egg.hatching", eggEvent.isHatching());
            collector.putString("egg.type", eggEvent.getHatchingType().name());
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
