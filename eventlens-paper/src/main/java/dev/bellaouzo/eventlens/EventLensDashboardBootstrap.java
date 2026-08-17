package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.application.DashboardQueryService;
import dev.bellaouzo.eventlens.application.DashboardStreamHub;
import dev.bellaouzo.eventlens.application.EventLensDashboardConfig;
import dev.bellaouzo.eventlens.paper.dashboard.PaperDashboardHttpServer;
import org.bukkit.plugin.java.JavaPlugin;

final class EventLensDashboardBootstrap {

    private EventLensDashboardBootstrap() {}

    static PaperDashboardHttpServer register(
            JavaPlugin plugin,
            EventLensDashboardConfig config,
            DashboardQueryService dashboardQueryService,
            DashboardStreamHub streamHub) {
        PaperDashboardHttpServer server =
                new PaperDashboardHttpServer(plugin, config, dashboardQueryService, streamHub);
        try {
            server.start();
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to start EventLens dashboard: " + ex.getMessage());
        }
        return server;
    }
}
