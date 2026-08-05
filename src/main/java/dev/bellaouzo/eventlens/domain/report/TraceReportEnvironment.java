package dev.bellaouzo.eventlens.domain.report;

import java.util.Map;
import org.jspecify.annotations.NonNull;

public record TraceReportEnvironment(
        @NonNull String serverVersion,
        @NonNull String javaVersion,
        @NonNull String paperVersion,
        @NonNull String eventLensVersion,
        @NonNull String platformLabel,
        @NonNull Map<String, String> pluginVersions,
        long generatedAtMillis) {

    public TraceReportEnvironment {
        pluginVersions = pluginVersions == null ? Map.of() : Map.copyOf(pluginVersions);
    }
}
