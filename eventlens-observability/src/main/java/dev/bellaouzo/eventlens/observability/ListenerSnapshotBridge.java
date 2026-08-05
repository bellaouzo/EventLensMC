package dev.bellaouzo.eventlens.observability;

@FunctionalInterface
public interface ListenerSnapshotBridge {

    CompactEventSnapshot capture(Object event, String checkpoint);
}
