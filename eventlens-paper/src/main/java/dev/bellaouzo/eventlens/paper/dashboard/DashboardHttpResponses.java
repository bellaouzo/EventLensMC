package dev.bellaouzo.eventlens.paper.dashboard;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class DashboardHttpResponses {

    static final String TEXT_PLAIN_UTF8 = "text/plain; charset=utf-8";

    private DashboardHttpResponses() {}

    static void writeJson(HttpExchange exchange, String body) throws IOException {
        write(exchange, 200, body, "application/json; charset=utf-8");
    }

    static void writePlain(HttpExchange exchange, int status, String body) throws IOException {
        write(exchange, status, body, TEXT_PLAIN_UTF8);
    }

    static void write(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
