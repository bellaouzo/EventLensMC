package dev.bellaouzo.eventlens.paper.dashboard;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import dev.bellaouzo.eventlens.application.DashboardAccess;
import dev.bellaouzo.eventlens.application.EventLensDashboardConfig;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

final class DashboardAuthFilter extends Filter {

    private final EventLensDashboardConfig config;

    DashboardAuthFilter(EventLensDashboardConfig config) {
        this.config = config;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        if (!config.hasToken()) {
            chain.doFilter(exchange);
            return;
        }
        String authorization = firstHeader(exchange, "Authorization");
        String cookie = firstHeader(exchange, "Cookie");
        String rawQuery = exchange.getRequestURI().getRawQuery();
        Optional<String> presented = DashboardAccess.presentedToken(authorization, cookie, rawQuery);
        if (!config.tokenMatches(presented.orElse(null))) {
            DashboardHttpResponses.writePlain(exchange, 401, "Unauthorized");
            return;
        }
        if (DashboardAccess.queryToken(rawQuery) != null) {
            exchange.getResponseHeaders().add("Set-Cookie", DashboardAccess.sessionCookie(config.token()));
        }
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "EventLens dashboard token";
    }

    private static String firstHeader(HttpExchange exchange, String name) {
        List<String> values = exchange.getRequestHeaders().get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }
}
