package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.observability.PerformanceBudget;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import java.util.Map;
import java.util.Optional;

public record EventLensCommandConfig(
        boolean devMode,
        OutputDetailLevel defaultDetailLevel,
        long defaultSlowThresholdNanos,
        boolean requireHotEventConfirmation,
        boolean showPerformanceWarnings,
        int maxRecentTraces,
        int maxFavorites,
        Map<String, TracePreset> presets,
        Optional<String> autoBaselineCompare) {

    public EventLensCommandConfig {
        presets = presets == null ? Map.of() : Map.copyOf(presets);
    }

    public static EventLensCommandConfig defaults() {
        return new EventLensCommandConfig(
                false,
                OutputDetailLevel.NORMAL,
                PerformanceBudget.DEFAULT_SLOW_THRESHOLD_NANOS,
                true,
                true,
                20,
                32,
                Map.of(),
                Optional.empty());
    }

    public Optional<TracePreset> preset(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(presets.get(name.toLowerCase(java.util.Locale.ROOT)));
    }
}
