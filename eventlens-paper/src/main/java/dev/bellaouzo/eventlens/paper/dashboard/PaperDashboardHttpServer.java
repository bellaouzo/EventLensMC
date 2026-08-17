package dev.bellaouzo.eventlens.paper.dashboard;

import com.sun.net.httpserver.HttpServer;
import dev.bellaouzo.eventlens.application.DashboardQueryService;
import dev.bellaouzo.eventlens.application.DashboardStreamHub;
import dev.bellaouzo.eventlens.application.EventLensDashboardConfig;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperDashboardHttpServer implements AutoCloseable {

    private final JavaPlugin plugin;
    private final EventLensDashboardConfig config;
    private final DashboardQueryService dashboardQueryService;
    private final DashboardStreamHub streamHub;
    private HttpServer httpServer;

    public PaperDashboardHttpServer(
            JavaPlugin plugin,
            EventLensDashboardConfig config,
            DashboardQueryService dashboardQueryService,
            DashboardStreamHub streamHub) {
        this.plugin = plugin;
        this.config = config;
        this.dashboardQueryService = dashboardQueryService;
        this.streamHub = streamHub;
    }

    public void start() throws IOException {
        if (!config.enabled()) {
            return;
        }
        if (!config.isLoopbackOnly()) {
            plugin.getLogger().warning("Dashboard bind address must be loopback-only. Dashboard not started.");
            return;
        }

        InetSocketAddress address = new InetSocketAddress(config.bindAddress(), config.port());
        httpServer = HttpServer.create(address, 0);
        httpServer.createContext("/api/stream", new DashboardSseHandler(streamHub));
        httpServer.createContext("/api/", new DashboardApiHandler(plugin, dashboardQueryService));
        httpServer.createContext("/", new DashboardStaticHandler());
        httpServer.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "EventLens-Dashboard");
            thread.setDaemon(true);
            return thread;
        }));
        httpServer.start();

        plugin.getLogger()
                .info(() -> "EventLens dashboard listening on http://" + config.bindAddress() + ":" + config.port());
    }

    @Override
    public void close() {
        if (httpServer == null) {
            return;
        }
        httpServer.stop(0);
        httpServer = null;
        plugin.getLogger().info("EventLens dashboard stopped.");
    }
}
