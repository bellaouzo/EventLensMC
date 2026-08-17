package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TracePresetMergerTest {

    @Test
    void mergesPresetValuesAndAllowsExplicitOverrides() {
        EventLensCommandConfig config = new EventLensCommandConfig(
                false,
                EventLensCommandConfig.defaults().defaultDetailLevel(),
                1_000_000L,
                true,
                true,
                20,
                32,
                Map.of(
                        "quick",
                        new TracePreset(
                                "quick",
                                java.util.Optional.of("EventLens"),
                                java.util.Optional.empty(),
                                java.util.Optional.empty(),
                                java.util.Optional.of(30_000L),
                                java.util.Optional.of(64),
                                java.util.Optional.empty(),
                                false,
                                List.of())));

        TracePresetMerger.MergeResult result =
                TracePresetMerger.merge(config, List.of("--preset", "quick", "--player", "Steve"));

        assertFalse(result.hasError());
        assertTrue(result.tokens().contains("--plugin"));
        assertTrue(result.tokens().contains("EventLens"));
        assertTrue(result.tokens().contains("--player"));
        assertTrue(result.tokens().contains("Steve"));
        assertTrue(result.tokens().contains("--max-events"));
        assertTrue(result.tokens().contains("64"));
    }

    @Test
    void mergeRemovesPresetFlagAndNameFromTokens() {
        EventLensCommandConfig config = new EventLensCommandConfig(
                false,
                EventLensCommandConfig.defaults().defaultDetailLevel(),
                1_000_000L,
                true,
                true,
                20,
                32,
                Map.of(
                        "quick-interact",
                        new TracePreset(
                                "quick-interact",
                                java.util.Optional.empty(),
                                java.util.Optional.empty(),
                                java.util.Optional.empty(),
                                java.util.Optional.of(30_000L),
                                java.util.Optional.of(128),
                                java.util.Optional.of(1_000_000L),
                                false,
                                List.of())));

        TracePresetMerger.MergeResult result = TracePresetMerger.merge(config, List.of("--preset", "quick-interact"));

        assertFalse(result.hasError());
        assertFalse(result.tokens().contains("--preset"));
        assertFalse(result.tokens().contains("quick-interact"));
        assertTrue(result.tokens().contains("--max-duration"));
        assertTrue(result.tokens().contains("30000ms"));
    }

    @Test
    void mergedPresetTokensParseDurationWithMillisSuffix() {
        EventLensCommandConfig config = EventLensCommandConfig.defaults();
        TracePresetMerger.MergeResult mergeResult = TracePresetMerger.merge(
                new EventLensCommandConfig(
                        false,
                        config.defaultDetailLevel(),
                        config.defaultSlowThresholdNanos(),
                        true,
                        true,
                        20,
                        32,
                        Map.of(
                                "quick-interact",
                                new TracePreset(
                                        "quick-interact",
                                        java.util.Optional.empty(),
                                        java.util.Optional.empty(),
                                        java.util.Optional.empty(),
                                        java.util.Optional.of(30_000L),
                                        java.util.Optional.of(128),
                                        java.util.Optional.empty(),
                                        false,
                                        List.of()))),
                List.of("--preset", "quick-interact"));

        TraceCommandService.TraceStartOptions options =
                TraceCommandService.TraceStartOptions.parse(mergeResult.tokens(), config);

        assertEquals(30_000L, options.maxDurationMillis().orElseThrow());
    }

    @Test
    void reportsUnknownPreset() {
        TracePresetMerger.MergeResult result =
                TracePresetMerger.merge(EventLensCommandConfig.defaults(), List.of("--preset", "missing"));

        assertTrue(result.hasError());
        assertEquals(
                "Unknown trace preset \"missing\".",
                result.presetNotFoundError().orElseThrow());
    }
}
