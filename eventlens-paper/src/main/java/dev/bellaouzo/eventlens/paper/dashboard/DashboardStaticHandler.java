package dev.bellaouzo.eventlens.paper.dashboard;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

final class DashboardStaticHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            DashboardHttpResponses.writePlain(exchange, 405, "Method not allowed");
            return;
        }
        DashboardStaticAssets.AssetResponse asset = DashboardStaticAssets.load(exchange.getRequestURI());
        DashboardHttpResponses.write(exchange, asset.statusCode(), asset.body(), asset.contentType());
    }
}
