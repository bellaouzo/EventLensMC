package dev.bellaouzo.eventlens.paper.dashboard;

import com.sun.net.httpserver.HttpExchange;
import dev.bellaouzo.eventlens.application.DashboardQueryService;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

final class DashboardApiRoutes {

    private DashboardApiRoutes() {}

    static void dispatch(
            HttpExchange exchange,
            URI uri,
            String path,
            DashboardQueryService dashboardQueryService,
            DashboardMainThreadBridge mainThreadBridge)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        if (path.equals("/api/status")) {
            DashboardHttpResponses.writeJson(exchange, mainThreadBridge.run(dashboardQueryService::statusJson));
            return;
        }
        if (path.equals("/api/sessions")) {
            DashboardHttpResponses.writeJson(exchange, mainThreadBridge.run(dashboardQueryService::sessionsJson));
            return;
        }
        if (path.startsWith("/api/sessions/") && path.endsWith("/report")) {
            handleSessionReport(exchange, path, dashboardQueryService, mainThreadBridge);
            return;
        }
        if (path.equals("/api/reports")) {
            DashboardHttpResponses.writeJson(exchange, dashboardQueryService.reportsJson());
            return;
        }
        if (path.startsWith("/api/reports/")) {
            handleReportFile(exchange, path, dashboardQueryService);
            return;
        }
        if (path.equals("/api/graph/events")) {
            DashboardHttpResponses.writeJson(
                    exchange, mainThreadBridge.run(dashboardQueryService::eventRelationshipGraphJson));
            return;
        }
        if (path.equals("/api/graph/plugins")) {
            Optional<String> sessionId = queryValue(uri, "session");
            DashboardHttpResponses.writeJson(
                    exchange, mainThreadBridge.run(() -> dashboardQueryService.pluginInteractionGraphJson(sessionId)));
            return;
        }
        DashboardHttpResponses.writePlain(exchange, 404, "Not found");
    }

    private static void handleSessionReport(
            HttpExchange exchange,
            String path,
            DashboardQueryService dashboardQueryService,
            DashboardMainThreadBridge mainThreadBridge)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        String sessionId = path.substring("/api/sessions/".length(), path.length() - "/report".length());
        Optional<String> report = mainThreadBridge.run(() -> dashboardQueryService.sessionReportJson(sessionId));
        if (report.isEmpty()) {
            DashboardHttpResponses.writePlain(exchange, 404, "Session not found");
            return;
        }
        DashboardHttpResponses.writeJson(exchange, report.get());
    }

    private static void handleReportFile(
            HttpExchange exchange, String path, DashboardQueryService dashboardQueryService) throws IOException {
        String fileName = path.substring("/api/reports/".length());
        Optional<String> content = dashboardQueryService.readReportFile(fileName);
        if (content.isEmpty()) {
            DashboardHttpResponses.writePlain(exchange, 404, "Report not found");
            return;
        }
        DashboardHttpResponses.writeJson(exchange, content.get());
    }

    private static Optional<String> queryValue(URI uri, String key) {
        String query = uri.getQuery();
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        int searchFrom = 0;
        while (searchFrom < query.length()) {
            int ampersand = query.indexOf('&', searchFrom);
            String part = ampersand < 0 ? query.substring(searchFrom) : query.substring(searchFrom, ampersand);
            int equals = part.indexOf('=');
            if (equals > 0 && part.substring(0, equals).equals(key)) {
                return Optional.of(part.substring(equals + 1));
            }
            if (ampersand < 0) {
                break;
            }
            searchFrom = ampersand + 1;
        }
        return Optional.empty();
    }
}
