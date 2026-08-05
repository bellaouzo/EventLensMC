package dev.bellaouzo.eventlens.domain.snapshot;

import java.util.List;

public record EventSnapshot(
        String eventClassName,
        String checkpoint,
        long capturedAtMillis,
        long capturedAtNanos,
        List<SnapshotField> fields) {

    public EventSnapshot(String eventClassName, String checkpoint, long capturedAtMillis, List<SnapshotField> fields) {
        this(eventClassName, checkpoint, capturedAtMillis, 0L, fields);
    }
}
