package dev.bellaouzo.eventlens.paper.snapshot;

import dev.bellaouzo.eventlens.domain.snapshot.SnapshotLimits;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import java.util.LinkedHashMap;
import java.util.Map;

final class SnapshotFieldCollector {

    private final Map<String, SnapshotValue> fields = new LinkedHashMap<>();

    void putString(String name, String value) {
        if (fields.size() >= SnapshotLimits.MAX_FIELDS || fields.containsKey(name)) {
            return;
        }
        if (value == null) {
            fields.put(name, new SnapshotValue.Unsupported("null value"));
            return;
        }
        if (value.length() > SnapshotLimits.MAX_STRING_LENGTH) {
            fields.put(
                    name,
                    new SnapshotValue.Truncated(
                            value.substring(0, SnapshotLimits.MAX_STRING_LENGTH),
                            "exceeded " + SnapshotLimits.MAX_STRING_LENGTH + " characters"));
            return;
        }
        fields.put(name, new SnapshotValue.Present("string", value));
    }

    void putBoolean(String name, boolean value) {
        if (fields.size() >= SnapshotLimits.MAX_FIELDS || fields.containsKey(name)) {
            return;
        }
        fields.put(name, new SnapshotValue.Present("boolean", Boolean.toString(value)));
    }

    void putNumber(String name, Number value) {
        if (fields.size() >= SnapshotLimits.MAX_FIELDS || fields.containsKey(name)) {
            return;
        }
        if (value == null) {
            fields.put(name, new SnapshotValue.Unsupported("null value"));
            return;
        }
        fields.put(name, new SnapshotValue.Present("number", value.toString()));
    }

    void putUnsupported(String name, String reason) {
        if (fields.size() >= SnapshotLimits.MAX_FIELDS || fields.containsKey(name)) {
            return;
        }
        fields.put(name, new SnapshotValue.Unsupported(reason));
    }

    Map<String, SnapshotValue> fields() {
        return Map.copyOf(fields);
    }
}
