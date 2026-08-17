package dev.bellaouzo.eventlens.application;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DashboardStreamHub {

    public interface StreamClient {
        void send(String event, String data);

        void close();
    }

    private final CopyOnWriteArrayList<StreamClient> clients = new CopyOnWriteArrayList<>();

    public void subscribe(StreamClient client) {
        clients.add(client);
    }

    public void unsubscribe(StreamClient client) {
        clients.remove(client);
    }

    public void publish(String event, String data) {
        for (StreamClient client : clients) {
            try {
                client.send(event, data);
            } catch (RuntimeException ignored) {
                client.close();
                clients.remove(client);
            }
        }
    }

    public List<StreamClient> clientsSnapshot() {
        return List.copyOf(clients);
    }
}
