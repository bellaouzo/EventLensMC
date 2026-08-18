package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.raid.RaidEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.ItemStack;

final class WorldInventoryMechanicSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof WeatherChangeEvent weatherEvent) {
            collector.putBoolean("weather.toRain", weatherEvent.toWeatherState());
            return;
        }
        if (event instanceof LightningStrikeEvent lightningEvent) {
            collector.putString("lightning.cause", lightningEvent.getCause().name());
            return;
        }
        if (event instanceof PortalCreateEvent portalEvent) {
            collector.putString("portal.reason", portalEvent.getReason().name());
            collector.putNumber("portal.blocks", portalEvent.getBlocks().size());
            return;
        }
        if (event instanceof StructureGrowEvent growEvent) {
            collector.putString("grow.species", growEvent.getSpecies().name());
            if (growEvent.getPlayer() != null) {
                PlayerSnapshotFields.contribute(collector, growEvent.getPlayer());
            }
            return;
        }
        if (event instanceof RaidEvent raidEvent) {
            collector.putNumber("raid.badOmen", raidEvent.getRaid().getBadOmenLevel());
            collector.putNumber("raid.wave", raidEvent.getRaid().getSpawnedGroups());
            return;
        }
        if (event instanceof LootGenerateEvent lootEvent) {
            collector.putNumber("loot.size", lootEvent.getLoot().size());
            return;
        }
        if (event instanceof TimeSkipEvent skipEvent) {
            collector.putString("timeskip.reason", skipEvent.getSkipReason().name());
            collector.putNumber("timeskip.amount", skipEvent.getSkipAmount());
            return;
        }
        if (event instanceof PrepareAnvilEvent anvilEvent) {
            putItem(collector, "anvil.result", anvilEvent.getResult());
            return;
        }
        if (event instanceof EnchantItemEvent enchantEvent) {
            collector.putNumber("enchant.cost", enchantEvent.getExpLevelCost());
            collector.putNumber("enchant.count", enchantEvent.getEnchantsToAdd().size());
            return;
        }
        if (event instanceof BrewEvent brewEvent) {
            collector.putNumber("brew.fuel", brewEvent.getFuelLevel());
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
