package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.observability.CompactEventSnapshot;
import dev.bellaouzo.eventlens.observability.CompactField;
import java.util.List;

final class ModCompactSnapshotConverter {

    private ModCompactSnapshotConverter() {}

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

    private static SnapshotValue toSnapshotValue(CompactField field) {
        if ("unsupported".equals(field.type())) {
            return new SnapshotValue.Unsupported(field.display());
        }
        if ("truncated".equals(field.type())) {
            return new SnapshotValue.Truncated(field.display(), "compact snapshot");
        }
        return new SnapshotValue.Present(field.type(), field.display());
    }
}
