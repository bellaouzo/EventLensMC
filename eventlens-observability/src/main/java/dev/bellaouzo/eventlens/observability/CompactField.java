package dev.bellaouzo.eventlens.observability;

import java.util.List;
import java.util.Objects;

public record CompactField(String name, String type, String display) {

    public CompactField {
        name = Objects.requireNonNullElse(name, "");
        type = Objects.requireNonNullElse(type, "");
        display = Objects.requireNonNullElse(display, "");
    }
}
