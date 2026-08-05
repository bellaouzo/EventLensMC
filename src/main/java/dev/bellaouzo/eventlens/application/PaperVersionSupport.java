package dev.bellaouzo.eventlens.application;

import java.util.Locale;

final class PaperVersionSupport {

    private PaperVersionSupport() {}

    static boolean isCompatible(String expectedPlatform, String paperVersionReported, String bukkitVersionReported) {
        if (expectedPlatform == null || expectedPlatform.isBlank()) {
            return true;
        }
        String normalizedPaper = normalize(paperVersionReported);
        String normalizedBukkit = normalize(bukkitVersionReported);
        if (expectedPlatform.contains("26.2")) {
            return normalizedPaper.contains("26.2") || normalizedBukkit.contains("1.21");
        }
        return true;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
