package dev.bellaouzo.eventlens.paper.snapshot;

import org.bukkit.event.Event;

interface EventSnapshotAdapter {

    void contribute(Event event, SnapshotFieldCollector collector);
}
