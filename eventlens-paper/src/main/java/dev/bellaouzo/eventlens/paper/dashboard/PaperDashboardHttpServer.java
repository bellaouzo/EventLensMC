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
        if (!config.canBind()) {
            plugin.getLogger()
                    .warning("Dashboard bind-address is not localhost. Set dashboard.token to 12+ characters"
                            + " in config.yml to allow remote access, or set dashboard.bind-address"
                            + " back to 127.0.0.1. Hosts that cannot open a port should use"
                            + " /eventlens trace export --format bundle and download the .zip.");
            return;
        }

        InetSocketAddress address = new InetSocketAddress(config.bindAddress(), config.port());
        httpServer = HttpServer.create(address, 0);
        httpServer
                .createContext("/api/stream", new DashboardSseHandler(streamHub))
                .getFilters()
                .add(new DashboardAuthFilter(config));
        httpServer
                .createContext("/api/", new DashboardApiHandler(plugin, dashboardQueryService))
                .getFilters()
                .add(new DashboardAuthFilter(config));
        httpServer.createContext("/", new DashboardStaticHandler()).getFilters().add(new DashboardAuthFilter(config));
        httpServer.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "EventLens-Dashboard");
            thread.setDaemon(true);
            return thread;
        }));
        httpServer.start();

        if (config.isLoopbackOnly()) {
            plugin.getLogger()
                    .info(() ->
                            "EventLens dashboard listening on http://" + config.bindAddress() + ":" + config.port());
        } else {
            plugin.getLogger()
                    .info(() -> "EventLens dashboard listening on http://"
                            + config.bindAddress()
                            + ":"
                            + config.port()
                            + " (token required; open http://<host>:"
                            + config.port()
                            + "/?token=...). dashboard.token is not printed here.");
        }
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
