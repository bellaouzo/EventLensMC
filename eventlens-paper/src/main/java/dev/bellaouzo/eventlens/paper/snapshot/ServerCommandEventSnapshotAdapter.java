package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;
import org.bukkit.event.server.ServerCommandEvent;

final class ServerCommandEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof ServerCommandEvent commandEvent)) {
            return;
        }
        collector.putString("command.sender", commandEvent.getSender().getName());
        collector.putString("command.line", commandEvent.getCommand());
    }
}
