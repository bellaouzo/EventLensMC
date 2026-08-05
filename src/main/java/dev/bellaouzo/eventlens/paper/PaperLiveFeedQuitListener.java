package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.TraceLiveFeedService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PaperLiveFeedQuitListener implements Listener {

    private final TraceLiveFeedService liveFeedService;

    public PaperLiveFeedQuitListener(TraceLiveFeedService liveFeedService) {
        this.liveFeedService = liveFeedService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        liveFeedService.onViewerDisconnect(event.getPlayer().getName());
    }
}
