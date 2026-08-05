package dev.bellaouzo.eventlens.observability;

import java.util.List;
import java.util.Objects;

public record CompactEventSnapshot(
        String eventClassName, String checkpoint, long capturedAtNanos, List<CompactField> fields) {

    public CompactEventSnapshot {
        eventClassName = Objects.requireNonNullElse(eventClassName, "unknown");
        checkpoint = Objects.requireNonNullElse(checkpoint, "unknown");
        fields = fields == null ? List.of() : List.copyOf(fields);
    }

    public static CompactEventSnapshot empty(String checkpoint) {
        return new CompactEventSnapshot("unknown", checkpoint, 0L, List.of());
    }
}
