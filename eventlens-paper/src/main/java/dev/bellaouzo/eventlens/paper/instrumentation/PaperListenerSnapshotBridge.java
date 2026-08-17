package dev.bellaouzo.eventlens.paper.instrumentation;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.paper.snapshot.PaperEventSnapshotCapture;
import org.bukkit.event.Event;

final class PaperListenerSnapshotBridge implements dev.bellaouzo.eventlens.observability.ListenerSnapshotBridge {

    private final PaperEventSnapshotCapture capture = new PaperEventSnapshotCapture();

    @Override
    public dev.bellaouzo.eventlens.observability.CompactEventSnapshot capture(Object event, String checkpoint) {
        if (!(event instanceof Event bukkitEvent)) {
            return dev.bellaouzo.eventlens.observability.CompactEventSnapshot.empty(checkpoint);
        }
        EventSnapshot snapshot = capture.capture(bukkitEvent, checkpoint);
        return CompactSnapshotConverter.fromEventSnapshot(snapshot);
    }
}
