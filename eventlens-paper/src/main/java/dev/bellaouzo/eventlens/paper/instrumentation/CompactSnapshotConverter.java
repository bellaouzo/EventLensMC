package dev.bellaouzo.eventlens.paper.instrumentation;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.observability.CompactEventSnapshot;
import dev.bellaouzo.eventlens.observability.CompactField;
import java.util.List;

final class CompactSnapshotConverter {

    private CompactSnapshotConverter() {}

    static EventSnapshot toEventSnapshot(CompactEventSnapshot compact) {
        if (compact == null) {
            return null;
        }
        List<SnapshotField> fields = compact.fields().stream()
                .map(field -> new SnapshotField(field.name(), toSnapshotValue(field)))
                .toList();
        return new EventSnapshot(
                compact.eventClassName(),
                compact.checkpoint(),
                System.currentTimeMillis(),
                compact.capturedAtNanos(),
                fields);
    }

    static CompactEventSnapshot fromEventSnapshot(EventSnapshot snapshot) {
        List<CompactField> fields = snapshot.fields().stream()
                .map(field -> new CompactField(field.name(), fieldType(field.value()), fieldDisplay(field.value())))
                .toList();
        return new CompactEventSnapshot(
                snapshot.eventClassName(), snapshot.checkpoint(), snapshot.capturedAtNanos(), fields);
    }

    private static SnapshotValue toSnapshotValue(CompactField field) {
        if ("unsupported".equals(field.type())) {
            return new SnapshotValue.Unsupported(field.display());
        }
        if ("truncated".equals(field.type())) {
            return new SnapshotValue.Truncated(field.display(), "compact snapshot");
        }
        return new SnapshotValue.Present(field.type(), field.display());
    }

    private static String fieldType(SnapshotValue value) {
        if (value instanceof SnapshotValue.Present present) {
            return present.type();
        }
        if (value instanceof SnapshotValue.Unsupported) {
            return "unsupported";
        }
        if (value instanceof SnapshotValue.Truncated truncated) {
            return "truncated:" + truncated.reason();
        }
        return "unknown";
    }

    private static String fieldDisplay(SnapshotValue value) {
        return switch (value) {
            case SnapshotValue.Present present -> present.display();
            case SnapshotValue.Unsupported(var reason) -> reason;
            case SnapshotValue.Truncated truncated -> truncated.display();
        };
    }
}
