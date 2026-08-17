package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardStreamHubTest {

    @Test
    void publishesEventsToSubscribers() {
        DashboardStreamHub hub = new DashboardStreamHub();
        List<String> received = new ArrayList<>();

        DashboardStreamHub.StreamClient client = new DashboardStreamHub.StreamClient() {
            @Override
            public void send(String event, String data) {
                received.add(event + ":" + data);
            }

            @Override
            public void close() {
                // no-op for test client
            }
        };

        hub.subscribe(client);
        hub.publish("dispatch", "{\"sessionId\":\"abc123\"}");

        assertEquals(List.of("dispatch:{\"sessionId\":\"abc123\"}"), received);
    }
}
