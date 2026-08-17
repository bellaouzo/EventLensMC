package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.application.LiveFeedConfig;
import dev.bellaouzo.eventlens.application.TraceLiveFeedService;
import dev.bellaouzo.eventlens.paper.PaperLiveFeedAdapter;
import dev.bellaouzo.eventlens.paper.PaperLiveFeedQuitListener;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import org.bukkit.plugin.java.JavaPlugin;

final class EventLensLiveFeedBootstrap {

    private EventLensLiveFeedBootstrap() {}

    static TraceLiveFeedService register(
            JavaPlugin plugin, TraceSessionManager traceSessionManager, LiveFeedConfig config) {
        PaperLiveFeedAdapter liveFeedAdapter = new PaperLiveFeedAdapter(plugin);
        TraceLiveFeedService traceLiveFeedService =
                new TraceLiveFeedService(traceSessionManager, liveFeedAdapter, config);
        plugin.getServer()
                .getPluginManager()
                .registerEvents(new PaperLiveFeedQuitListener(traceLiveFeedService), plugin);
        return traceLiveFeedService;
    }
}
