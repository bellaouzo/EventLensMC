package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.application.port.EnvironmentPort;
import dev.bellaouzo.eventlens.domain.report.TraceReportEnvironment;
import dev.bellaouzo.eventlens.modcommon.port.ModEnvironmentPort;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ModEnvironmentCollector implements EnvironmentPort {

    private final ModEnvironmentPort modEnvironmentPort;

    public ModEnvironmentCollector(ModEnvironmentPort modEnvironmentPort) {
        this.modEnvironmentPort = modEnvironmentPort;
    }

    @Override
    public TraceReportEnvironment capture(Set<String> relevantPluginNames, long generatedAtMillis) {
        Map<String, String> modVersions = new LinkedHashMap<>(modEnvironmentPort.loadedModVersions());
        for (String modId : relevantPluginNames) {
            modVersions.putIfAbsent(modId, modEnvironmentPort.loadedModVersions().getOrDefault(modId, "unknown"));
        }
        modVersions.putIfAbsent("eventlens", modEnvironmentPort.eventLensVersion());

        return new TraceReportEnvironment(
                modEnvironmentPort.minecraftVersion(),
                System.getProperty("java.version", "unknown"),
                "",
                modEnvironmentPort.eventLensVersion(),
                modEnvironmentPort.platformLabel(),
                Map.of(),
                generatedAtMillis,
                modEnvironmentPort.runtimeKind(),
                modEnvironmentPort.loaderVersion(),
                modVersions);
    }
}
