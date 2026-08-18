package dev.bellaouzo.eventlens.domain.report;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum ExportFormat {
    JSON("json"),
    NDJSON("ndjson"),
    TEXT("txt"),
    HTML("html"),
    BUNDLE("bundle");

    public static final String VALID_TYPES_DESCRIPTION = "json, ndjson, text, txt, html, bundle";

    private final String extension;

    ExportFormat(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }

    public static Optional<ExportFormat> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("text".equals(normalized)) {
            return Optional.of(TEXT);
        }
        for (ExportFormat format : values()) {
            if (format.name().equalsIgnoreCase(normalized) || format.extension.equals(normalized)) {
                return Optional.of(format);
            }
        }
        return Optional.empty();
    }

    public static List<String> tabCompletionValues() {
        return List.of("json", "ndjson", "text", "txt", "html", "bundle");
    }

    public static String validTypesDescription() {
        return VALID_TYPES_DESCRIPTION;
    }
}
