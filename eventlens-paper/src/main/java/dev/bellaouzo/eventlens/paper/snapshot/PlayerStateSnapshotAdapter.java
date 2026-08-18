package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

final class PlayerStateSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (event instanceof PlayerToggleSneakEvent sneakEvent) {
            collector.putBoolean("sneaking", sneakEvent.isSneaking());
            return;
        }
        if (event instanceof PlayerToggleSprintEvent sprintEvent) {
            collector.putBoolean("sprinting", sprintEvent.isSprinting());
            return;
        }
        if (event instanceof PlayerToggleFlightEvent flightEvent) {
            collector.putBoolean("flying", flightEvent.isFlying());
            return;
        }
        if (event instanceof PlayerGameModeChangeEvent gameModeEvent) {
            collector.putString("gamemode", gameModeEvent.getNewGameMode().name());
            return;
        }
        if (event instanceof PlayerItemHeldEvent heldEvent) {
            collector.putNumber("slot.previous", heldEvent.getPreviousSlot());
            collector.putNumber("slot.new", heldEvent.getNewSlot());
            return;
        }
        if (event instanceof PlayerExpChangeEvent expEvent) {
            collector.putNumber("exp.amount", expEvent.getAmount());
            return;
        }
        if (event instanceof PlayerLevelChangeEvent levelEvent) {
            collector.putNumber("level.old", levelEvent.getOldLevel());
            collector.putNumber("level.new", levelEvent.getNewLevel());
            return;
        }
        if (event instanceof PlayerAnimationEvent animationEvent) {
            collector.putString("animation", animationEvent.getAnimationType().name());
            return;
        }
        if (event instanceof PlayerBedEnterEvent bedEvent) {
            collector.putString("bed.use", bedEvent.useBed().name());
            return;
        }
        if (event instanceof PlayerResourcePackStatusEvent packEvent) {
            collector.putString("pack.status", packEvent.getStatus().name());
            return;
        }
        if (event instanceof PlayerAdvancementDoneEvent advancementEvent) {
            collector.putString(
                    "advancement", advancementEvent.getAdvancement().getKey().toString());
        }
    }
}
