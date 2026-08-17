package dev.bellaouzo.eventlens.paper.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

final class DashboardStaticAssets {

    private static final String RESOURCE_PREFIX = "/dashboard/";

    private DashboardStaticAssets() {}

    static AssetResponse load(URI requestUri) {
        String path = requestUri.getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            return loadResource("index.html", "text/html; charset=utf-8");
        }
        if (path.startsWith("/assets/")) {
            String resourcePath = path.substring(1);
            return loadResource(resourcePath, contentType(resourcePath));
        }
        if (path.equals("/favicon.ico")) {
            return AssetResponse.notFound();
        }
        return AssetResponse.notFound();
    }

    private static AssetResponse loadResource(String relativePath, String contentType) {
        String resourceName = RESOURCE_PREFIX + relativePath;
        InputStream stream = DashboardStaticAssets.class.getResourceAsStream(resourceName);
        if (stream == null) {
            return AssetResponse.notFound();
        }
        try (stream) {
            return AssetResponse.ok(new String(stream.readAllBytes(), StandardCharsets.UTF_8), contentType);
        } catch (IOException _) {
            return AssetResponse.notFound();
        }
    }

    private static String contentType(String path) {
        if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (path.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (path.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        return "text/plain; charset=utf-8";
    }

    record AssetResponse(int statusCode, String body, String contentType) {

        static AssetResponse ok(String body, String contentType) {
            return new AssetResponse(200, body, contentType);
        }

        static AssetResponse notFound() {
            return new AssetResponse(404, "Not found", "text/plain; charset=utf-8");
        }
    }
}
