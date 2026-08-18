package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

final class CombatAndExplosionSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof EntityDamageByEntityEvent damageEvent) {
            collector.putString(
                    "damager.type", damageEvent.getDamager().getType().name());
            if (damageEvent.getDamager() instanceof org.bukkit.entity.Player player) {
                collector.putString("damager.name", player.getName());
            }
            collector.putBoolean("damage.critical", damageEvent.isCritical());
            return;
        }
        if (event instanceof EntityExplodeEvent explodeEvent) {
            EntitySnapshotFields.contribute(collector, explodeEvent.getEntity());
            LocationSnapshotFields.contribute(collector, "explode", explodeEvent.getLocation());
            collector.putNumber("explode.yield", explodeEvent.getYield());
            collector.putNumber("explode.blocks", explodeEvent.blockList().size());
            collector.putString(
                    "explode.result", explodeEvent.getExplosionResult().name());
            return;
        }
        if (event instanceof BlockExplodeEvent blockExplode) {
            collector.putNumber("explode.yield", blockExplode.getYield());
            collector.putNumber("explode.blocks", blockExplode.blockList().size());
            collector.putString(
                    "explode.result", blockExplode.getExplosionResult().name());
            return;
        }
        if (event instanceof ExplosionPrimeEvent primeEvent) {
            EntitySnapshotFields.contribute(collector, primeEvent.getEntity());
            collector.putNumber("explode.radius", primeEvent.getRadius());
            collector.putBoolean("explode.fire", primeEvent.getFire());
        }
    }
}
