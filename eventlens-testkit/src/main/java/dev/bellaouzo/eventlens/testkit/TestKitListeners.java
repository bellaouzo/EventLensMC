package dev.bellaouzo.eventlens.testkit;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

final class TestKitListeners implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onInteractLow(PlayerInteractEvent event) {
        if (!TestKitState.scenarioActive() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onInteractNormal(PlayerInteractEvent event) {
        if (!TestKitState.scenarioActive() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteractHigh(PlayerInteractEvent event) {
        if (!TestKitState.scenarioActive() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteractHighest(PlayerInteractEvent event) {
        if (!TestKitState.scenarioActive() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (TestKitState.mode() == TestKitMode.EXCEPTION) {
            throw new RuntimeException("EventLensTestTarget intentional exception");
        }
        if (TestKitState.mode() == TestKitMode.SLOW) {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteractMonitor(PlayerInteractEvent event) {
        // Observe-only checkpoint when scenario tracing is active.
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onBreakLow(BlockBreakEvent event) {
        if (!TestKitState.scenarioActive()) {
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBreakNormal(BlockBreakEvent event) {
        if (!TestKitState.scenarioActive()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBreakMonitor(BlockBreakEvent event) {
        // Observe-only checkpoint when scenario tracing is active.
    }
}
