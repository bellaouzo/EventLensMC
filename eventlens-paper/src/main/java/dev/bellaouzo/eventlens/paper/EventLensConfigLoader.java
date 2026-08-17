package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.EventLensDashboardConfig;
import dev.bellaouzo.eventlens.application.EventLensReportConfig;
import dev.bellaouzo.eventlens.application.LiveFeedConfig;
import dev.bellaouzo.eventlens.application.TracePreset;
import dev.bellaouzo.eventlens.domain.live.LiveFeedDisplayMode;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class EventLensConfigLoader {

    private EventLensConfigLoader() {}

    public static void mergeMissingDefaults(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        InputStream stream = plugin.getResource("config.yml");
        if (stream == null) {
            return;
        }
        FileConfiguration defaults =
                YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        FileConfiguration config = plugin.getConfig();
        config.setDefaults(defaults);
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public static EventLensReportConfig loadReportConfig(FileConfiguration config) {
        return new EventLensReportConfig(
                config.getInt(
                        "reports.retention-days",
                        EventLensReportConfig.defaults().retentionDays()),
                config.getBoolean(
                        "reports.auto-cleanup", EventLensReportConfig.defaults().autoCleanup()));
    }

    public static EventLensDashboardConfig loadDashboardConfig(FileConfiguration config) {
        EventLensDashboardConfig defaults = EventLensDashboardConfig.defaults();
        return new EventLensDashboardConfig(
                config.getBoolean("dashboard.enabled", defaults.enabled()),
                config.getInt("dashboard.port", defaults.port()),
                config.getString("dashboard.bind-address", defaults.bindAddress()));
    }

    public static EventLensCommandConfig loadCommandConfig(FileConfiguration config) {
        EventLensCommandConfig defaults = EventLensCommandConfig.defaults();
        boolean devMode = config.getBoolean("dev-mode", defaults.devMode());
        OutputDetailLevel detailLevel = OutputDetailLevel.parse(config.getString(
                "output.detail-level",
                devMode
                        ? OutputDetailLevel.VERBOSE.name()
                        : defaults.defaultDetailLevel().name()));
        long slowThresholdNanos = parseSlowThresholdNanos(
                config.getString("trace.slow-threshold-default", "1ms"), defaults.defaultSlowThresholdNanos());
        return new EventLensCommandConfig(
                devMode,
                detailLevel,
                slowThresholdNanos,
                !devMode
                        && config.getBoolean(
                                "trace.require-hot-event-confirmation", defaults.requireHotEventConfirmation()),
                config.getBoolean("trace.show-performance-warnings", defaults.showPerformanceWarnings()),
                config.getInt("preferences.max-recent-traces", defaults.maxRecentTraces()),
                config.getInt("preferences.max-favorites", defaults.maxFavorites()),
                parsePresets(config.getConfigurationSection("trace.presets")));
    }

    public static LiveFeedConfig loadLiveFeedConfig(FileConfiguration config) {
        LiveFeedConfig defaults = LiveFeedConfig.defaults();
        return new LiveFeedConfig(
                config.getLong("trace.live.aggregate-window-ms", defaults.aggregateWindowMillis()),
                config.getLong("trace.live.burst-window-ms", defaults.burstWindowMillis()),
                config.getInt("trace.live.burst-threshold", defaults.burstThreshold()),
                config.getInt("trace.live.max-lines-per-tick", defaults.maxLinesPerTick()),
                LiveFeedDisplayMode.parse(config.getString("trace.live.default-status-display", "actionbar")));
    }

    public static List<String> loadAdditionalTraceEvents(FileConfiguration config) {
        return config.getStringList("trace.additional-events");
    }

    private static Map<String, TracePreset> parsePresets(ConfigurationSection section) {
        if (section == null) {
            return Map.of();
        }
        Map<String, TracePreset> parsed = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection presetSection = section.getConfigurationSection(key);
            if (presetSection == null) {
                continue;
            }
            parsed.put(
                    key.toLowerCase(Locale.ROOT),
                    new TracePreset(
                            key,
                            optionalString(presetSection, "plugin"),
                            optionalString(presetSection, "player"),
                            optionalString(presetSection, "world"),
                            optionalLong(presetSection, "max-duration-ms"),
                            optionalInt(presetSection, "max-events"),
                            optionalLong(presetSection, "slow-threshold-ns"),
                            presetSection.getBoolean("capture-stacks", false),
                            Collections.emptyList()));
        }
        return Map.copyOf(parsed);
    }

    private static long parseSlowThresholdNanos(String value, long fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.endsWith("ms")) {
            double millis = Double.parseDouble(normalized.substring(0, normalized.length() - 2));
            return Math.max(1L, Math.round(millis * 1_000_000L));
        }
        if (normalized.endsWith("ns")) {
            return Math.max(1L, Long.parseLong(normalized.substring(0, normalized.length() - 2)));
        }
        double millis = Double.parseDouble(normalized);
        return Math.max(1L, Math.round(millis * 1_000_000L));
    }

    private static Optional<String> optionalString(ConfigurationSection section, String key) {
        String value = section.getString(key);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim());
    }

    private static Optional<Long> optionalLong(ConfigurationSection section, String key) {
        if (!section.contains(key)) {
            return Optional.empty();
        }
        return Optional.of(section.getLong(key));
    }

    private static Optional<Integer> optionalInt(ConfigurationSection section, String key) {
        if (!section.contains(key)) {
            return Optional.empty();
        }
        return Optional.of(section.getInt(key));
    }
}
