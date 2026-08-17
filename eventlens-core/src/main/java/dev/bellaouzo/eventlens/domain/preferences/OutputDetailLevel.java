package dev.bellaouzo.eventlens.domain.preferences;

import java.util.Locale;
import java.util.Optional;

public enum OutputDetailLevel {
    BRIEF,
    NORMAL,
    VERBOSE;

    public static OutputDetailLevel parse(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "brief", "minimal" -> BRIEF;
            case "verbose", "full" -> VERBOSE;
            default -> NORMAL;
        };
    }

    public static Optional<OutputDetailLevel> parseOptional(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(parse(value));
    }
}
