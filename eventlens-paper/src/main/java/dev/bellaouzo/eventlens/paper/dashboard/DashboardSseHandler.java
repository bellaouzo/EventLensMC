package dev.bellaouzo.eventlens.paper.dashboard;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.bellaouzo.eventlens.application.DashboardStreamHub;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class DashboardSseHandler implements HttpHandler {

    private static final SseEvent HEARTBEAT = new SseEvent("heartbeat", "{}");
    private static final SseEvent SHUTDOWN = new SseEvent("__shutdown__", "");

    private final DashboardStreamHub streamHub;

    DashboardSseHandler(DashboardStreamHub streamHub) {
        this.streamHub = streamHub;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            DashboardHttpResponses.writePlain(exchange, 405, "Method not allowed");
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.sendResponseHeaders(200, 0);

        OutputStream output = exchange.getResponseBody();
        AtomicBoolean open = new AtomicBoolean(true);
        BlockingQueue<SseEvent> outbound = new LinkedBlockingQueue<>();

        DashboardStreamHub.StreamClient client = new DashboardStreamHub.StreamClient() {
            @Override
            public void send(String event, String data) {
                if (!open.get()) {
                    return;
                }
                outbound.offer(new SseEvent(event, data));
            }

            @Override
            public void close() {
                if (!open.compareAndSet(true, false)) {
                    return;
                }
                outbound.offer(SHUTDOWN);
                streamHub.unsubscribe(this);
            }
        };

        streamHub.subscribe(client);
        outbound.offer(new SseEvent("connected", "{\"ok\":true}"));

        try {
            while (open.get()) {
                SseEvent event = outbound.poll(15, TimeUnit.SECONDS);
                if (event == null) {
                    writeEvent(output, HEARTBEAT.name(), HEARTBEAT.data());
                    continue;
                }
                if (SHUTDOWN.name().equals(event.name())) {
                    break;
                }
                writeEvent(output, event.name(), event.data());
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        } finally {
            client.close();
            try {
                output.close();
            } catch (IOException _) {
                // stream already closed
            }
            exchange.close();
        }
    }

    private static void writeEvent(OutputStream output, String event, String data) throws IOException {
        output.write(("event: " + event + "\n").getBytes(StandardCharsets.UTF_8));
        output.write(("data: " + data + "\n\n").getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    private record SseEvent(String name, String data) {}
}
