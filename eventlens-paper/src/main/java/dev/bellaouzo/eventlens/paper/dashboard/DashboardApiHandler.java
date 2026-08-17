package dev.bellaouzo.eventlens.paper.dashboard;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.bellaouzo.eventlens.application.DashboardQueryService;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.bukkit.plugin.java.JavaPlugin;

final class DashboardApiHandler implements HttpHandler {

    private final DashboardQueryService dashboardQueryService;
    private final DashboardMainThreadBridge mainThreadBridge;

    DashboardApiHandler(JavaPlugin plugin, DashboardQueryService dashboardQueryService) {
        this.dashboardQueryService = dashboardQueryService;
        this.mainThreadBridge = new DashboardMainThreadBridge(plugin);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            DashboardHttpResponses.writePlain(exchange, 405, "Method not allowed");
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath() == null ? "" : uri.getPath();
        try {
            DashboardApiRoutes.dispatch(exchange, uri, path, dashboardQueryService, mainThreadBridge);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            DashboardHttpResponses.writePlain(exchange, 500, "Internal error");
        } catch (ExecutionException | TimeoutException _) {
            DashboardHttpResponses.writePlain(exchange, 500, "Internal error");
        }
    }
}
