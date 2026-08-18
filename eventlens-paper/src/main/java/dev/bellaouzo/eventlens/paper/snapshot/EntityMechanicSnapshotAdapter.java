package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.PotionSplashEvent;

final class EntityMechanicSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof EntityRegainHealthEvent healthEvent) {
            collector.putNumber("heal.amount", healthEvent.getAmount());
            collector.putString("heal.reason", healthEvent.getRegainReason().name());
            return;
        }
        if (event instanceof EntityPotionEffectEvent potionEvent) {
            collector.putString("effect.action", potionEvent.getAction().name());
            collector.putString("effect.cause", potionEvent.getCause().name());
            if (potionEvent.getNewEffect() != null) {
                collector.putString(
                        "effect.type",
                        potionEvent.getNewEffect().getType().getKey().toString());
            }
            return;
        }
        if (event instanceof EntityTameEvent tameEvent) {
            if (tameEvent.getOwner() instanceof Player player) {
                PlayerSnapshotFields.contribute(collector, player);
            }
            return;
        }
        if (event instanceof EntityBreedEvent breedEvent) {
            if (breedEvent.getBreeder() instanceof Player player) {
                PlayerSnapshotFields.contribute(collector, player);
            }
            collector.putNumber("breed.exp", breedEvent.getExperience());
            return;
        }
        if (event instanceof EntityShootBowEvent bowEvent) {
            collector.putNumber("bow.force", bowEvent.getForce());
            if (bowEvent.getBow() != null) {
                collector.putString("bow.item", bowEvent.getBow().getType().name());
            }
            return;
        }
        contributeMovement(event, collector);
    }

    private static void contributeMovement(Event event, SnapshotFieldCollector collector) {
        if (event instanceof EntityTransformEvent transformEvent) {
            collector.putString(
                    "transform.reason", transformEvent.getTransformReason().name());
            return;
        }
        if (event instanceof EntityMountEvent mountEvent) {
            collector.putString("mount.type", mountEvent.getMount().getType().name());
            return;
        }
        if (event instanceof PotionSplashEvent splashEvent) {
            collector.putNumber(
                    "splash.affected", splashEvent.getAffectedEntities().size());
        }
    }
}
