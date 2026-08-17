package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;

public final class ModSnapshotFields {

    private static final int MAX_STRING = 512;

    private ModSnapshotFields() {}

    public static SnapshotField text(String name, String value) {
        String display = value == null ? "" : value;
        if (display.length() > MAX_STRING) {
            return new SnapshotField(
                    name, new SnapshotValue.Truncated(display.substring(0, MAX_STRING), "string_length"));
        }
        return new SnapshotField(name, new SnapshotValue.Present("string", display));
    }

    public static SnapshotField number(String name, Number value) {
        return new SnapshotField(name, new SnapshotValue.Present("number", String.valueOf(value)));
    }

    public static SnapshotField bool(String name, boolean value) {
        return new SnapshotField(name, new SnapshotValue.Present("boolean", Boolean.toString(value)));
    }
}
