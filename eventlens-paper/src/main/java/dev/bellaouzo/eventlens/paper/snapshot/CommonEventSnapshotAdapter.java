package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

final class CommonEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        collector.putString("event.class", event.getClass().getName());
        collector.putBoolean("event.async", event.isAsynchronous());
        collector.putString("event.name", event.getEventName());

        if (event instanceof Cancellable cancellable) {
            collector.putBoolean("cancelled", cancellable.isCancelled());
        } else {
            collector.putUnsupported("cancelled", "event is not cancellable");
        }
    }
}
