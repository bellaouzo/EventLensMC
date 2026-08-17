package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import java.util.Map;
import org.jspecify.annotations.NonNull;

public record TraceReportEnvironment(
        @NonNull String serverVersion,
        @NonNull String javaVersion,
        @NonNull String paperVersion,
        @NonNull String eventLensVersion,
        @NonNull String platformLabel,
        @NonNull Map<String, String> pluginVersions,
        long generatedAtMillis,
        @NonNull ModRuntimeKind runtimeKind,
        @NonNull String loaderVersion,
        @NonNull Map<String, String> modVersions) {

    public TraceReportEnvironment(
            @NonNull String serverVersion,
            @NonNull String javaVersion,
            @NonNull String paperVersion,
            @NonNull String eventLensVersion,
            @NonNull String platformLabel,
            @NonNull Map<String, String> pluginVersions,
            long generatedAtMillis) {
        this(
                serverVersion,
                javaVersion,
                paperVersion,
                eventLensVersion,
                platformLabel,
                pluginVersions,
                generatedAtMillis,
                ModRuntimeKind.PAPER,
                "",
                Map.of());
    }

    public TraceReportEnvironment {
        pluginVersions = pluginVersions == null ? Map.of() : Map.copyOf(pluginVersions);
        modVersions = modVersions == null ? Map.of() : Map.copyOf(modVersions);
        runtimeKind = runtimeKind == null ? ModRuntimeKind.PAPER : runtimeKind;
        loaderVersion = loaderVersion == null ? "" : loaderVersion;
    }
}
