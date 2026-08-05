package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.EnvironmentPort;
import dev.bellaouzo.eventlens.domain.report.TraceReportEnvironment;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class PaperEnvironmentCollector implements EnvironmentPort {

    private final Plugin plugin;
    private final String platformLabel;

    public PaperEnvironmentCollector(Plugin plugin, String platformLabel) {
        this.plugin = plugin;
        this.platformLabel = platformLabel;
    }

    @Override
    public TraceReportEnvironment capture(Set<String> relevantPluginNames, long generatedAtMillis) {
        Map<String, String> pluginVersions = new LinkedHashMap<>();
        for (String pluginName : relevantPluginNames) {
            Plugin resolved = Bukkit.getPluginManager().getPlugin(pluginName);
            if (resolved != null) {
                pluginVersions.put(pluginName, resolved.getPluginMeta().getVersion());
            }
        }
        pluginVersions.putIfAbsent("EventLens", plugin.getPluginMeta().getVersion());

        return new TraceReportEnvironment(
                Bukkit.getVersion(),
                System.getProperty("java.version", "unknown"),
                Bukkit.getVersion(),
                plugin.getPluginMeta().getVersion(),
                platformLabel,
                pluginVersions,
                generatedAtMillis);
    }
}
