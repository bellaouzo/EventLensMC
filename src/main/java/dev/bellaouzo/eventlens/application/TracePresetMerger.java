package dev.bellaouzo.eventlens.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class TracePresetMerger {

    private static final String PRESET_FLAG_PREFIX = "--preset=";

    private TracePresetMerger() {}

    public static MergeResult merge(EventLensCommandConfig config, List<String> tokens) {
        Optional<String> presetName = findPresetName(tokens);
        if (presetName.isEmpty()) {
            return new MergeResult(tokens, Optional.empty(), Optional.empty());
        }

        Optional<TracePreset> preset = config.preset(presetName.get());
        if (preset.isEmpty()) {
            return MergeResult.presetNotFound(presetName.get());
        }

        Map<String, String> explicitFlags = explicitFlags(tokens);
        List<String> merged = new ArrayList<>();
        TracePreset resolved = preset.get();
        resolved.pluginName().ifPresent(value -> addIfAbsent(merged, explicitFlags, "plugin", value));
        resolved.playerName().ifPresent(value -> addIfAbsent(merged, explicitFlags, "player", value));
        resolved.worldName().ifPresent(value -> addIfAbsent(merged, explicitFlags, "world", value));
        resolved.maxDurationMillis()
                .ifPresent(value -> addIfAbsent(merged, explicitFlags, "max-duration", value + "ms"));
        resolved.maxEventCount()
                .ifPresent(value -> addIfAbsent(merged, explicitFlags, "max-events", String.valueOf(value)));
        resolved.slowThresholdNanos()
                .ifPresent(value -> addIfAbsent(merged, explicitFlags, "slow-threshold", value + "ns"));
        if (resolved.captureStacks() && !explicitFlags.containsKey("capture-stacks")) {
            merged.add("--capture-stacks");
        }
        int index = 0;
        while (index < tokens.size()) {
            String token = tokens.get(index);
            if (token.equalsIgnoreCase("--preset")) {
                index += index + 1 < tokens.size() ? 2 : 1;
            } else if (token.toLowerCase(Locale.ROOT).startsWith(PRESET_FLAG_PREFIX)) {
                index++;
            } else {
                merged.add(token);
                index++;
            }
        }
        return new MergeResult(merged, presetName, Optional.empty());
    }

    private static void addIfAbsent(List<String> merged, Map<String, String> explicitFlags, String flag, String value) {
        if (!explicitFlags.containsKey(flag)) {
            merged.add("--" + flag);
            merged.add(value);
        }
    }

    private static Optional<String> findPresetName(List<String> tokens) {
        for (int index = 0; index < tokens.size(); index++) {
            String token = tokens.get(index);
            if (token.equalsIgnoreCase("--preset") && index + 1 < tokens.size()) {
                return Optional.of(tokens.get(index + 1));
            }
            if (token.toLowerCase(Locale.ROOT).startsWith(PRESET_FLAG_PREFIX)) {
                return Optional.of(token.substring(PRESET_FLAG_PREFIX.length()));
            }
        }
        return Optional.empty();
    }

    private static Map<String, String> explicitFlags(List<String> tokens) {
        Map<String, String> flags = new LinkedHashMap<>();
        int index = 0;
        while (index < tokens.size()) {
            index = readExplicitFlag(tokens, index, flags);
        }
        return flags;
    }

    private static int readExplicitFlag(List<String> tokens, int index, Map<String, String> flags) {
        String token = tokens.get(index);
        if (!token.startsWith("--")) {
            return index + 1;
        }
        String flag = token.substring(2).toLowerCase(Locale.ROOT);
        if (flag.equals("capture-stacks") || flag.equals("confirm-hot")) {
            flags.put(flag, "true");
            return index + 1;
        }
        if (flag.startsWith("preset")) {
            if (index + 1 < tokens.size() && !tokens.get(index + 1).startsWith("--")) {
                return index + 2;
            }
            return index + 1;
        }
        if (index + 1 < tokens.size() && !tokens.get(index + 1).startsWith("--")) {
            flags.put(flag, tokens.get(index + 1));
            return index + 2;
        }
        return index + 1;
    }

    public record MergeResult(List<String> tokens, Optional<String> presetName, Optional<String> presetNotFoundError) {

        public MergeResult {
            tokens = tokens == null ? List.of() : List.copyOf(tokens);
        }

        static MergeResult presetNotFound(String presetName) {
            return new MergeResult(
                    List.of(), Optional.of(presetName), Optional.of("Unknown trace preset \"" + presetName + "\"."));
        }

        public boolean hasError() {
            return presetNotFoundError.isPresent();
        }
    }
}
