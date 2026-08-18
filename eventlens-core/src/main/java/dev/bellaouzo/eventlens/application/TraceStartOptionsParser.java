package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import dev.bellaouzo.eventlens.domain.trace.TraceCancellationFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceRegion;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class TraceStartOptionsParser {

    private TraceStartOptionsParser() {}

    static TraceCommandService.TraceStartOptions parse(List<String> tokens) {
        return parse(tokens, EventLensCommandConfig.defaults());
    }

    static TraceCommandService.TraceStartOptions parse(List<String> tokens, EventLensCommandConfig commandConfig) {
        TraceFilter.Builder filterBuilder = TraceFilter.Builder.unrestricted();
        Optional<Long> maxDurationMillis = Optional.empty();
        Optional<Integer> maxEventCount = Optional.empty();
        long slowThresholdNanos = commandConfig.defaultSlowThresholdNanos();
        boolean captureStacks = false;
        boolean confirmHot = false;
        Optional<OutputDetailLevel> detailLevel = Optional.empty();
        boolean genericAllow = false;

        int index = 0;
        while (index < tokens.size()) {
            String token = tokens.get(index);
            if (!token.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + token);
            }

            String flag = token.substring(2).toLowerCase(Locale.ROOT);
            switch (flag) {
                case "plugin" -> {
                    filterBuilder.pluginName(requireValue(tokens, index, flag));
                    index += 2;
                }
                case "player" -> {
                    filterBuilder.playerName(requireValue(tokens, index, flag));
                    index += 2;
                }
                case "world" -> {
                    filterBuilder.worldName(requireValue(tokens, index, flag));
                    index += 2;
                }
                case "region" -> {
                    filterBuilder.region(parseRegion(requireValue(tokens, index, flag)));
                    index += 2;
                }
                case "cancelled" -> {
                    filterBuilder.cancellationFilter(parseCancellation(requireValue(tokens, index, flag)));
                    index += 2;
                }
                case "max-events" -> {
                    maxEventCount = Optional.of(Integer.parseInt(requireValue(tokens, index, flag)));
                    index += 2;
                }
                case "max-duration" -> {
                    maxDurationMillis = Optional.of(parseDurationMillis(requireValue(tokens, index, flag)));
                    index += 2;
                }
                case "slow-threshold" -> {
                    slowThresholdNanos = parseThresholdNanos(requireValue(tokens, index, flag));
                    index += 2;
                }
                case "capture-stacks" -> {
                    captureStacks = true;
                    index += 1;
                }
                case "confirm-hot" -> {
                    confirmHot = true;
                    index += 1;
                }
                case "generic" -> {
                    genericAllow = true;
                    index += 1;
                }
                case "detail" -> {
                    detailLevel = Optional.of(OutputDetailLevel.parse(requireValue(tokens, index, flag)));
                    index += 2;
                }
                case "preset" -> index += 2;
                default -> throw new IllegalArgumentException("Unknown option: " + token);
            }
        }

        return new TraceCommandService.TraceStartOptions(
                filterBuilder.build(),
                maxDurationMillis,
                maxEventCount,
                slowThresholdNanos,
                captureStacks,
                confirmHot,
                detailLevel,
                genericAllow);
    }

    private static String requireValue(List<String> tokens, int index, String flag) {
        if (index + 1 >= tokens.size()) {
            throw new IllegalArgumentException("Missing value for --" + flag);
        }
        return tokens.get(index + 1);
    }

    private static TraceRegion parseRegion(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Region must be x1,z1,x2,z2");
        }
        int x1 = Integer.parseInt(parts[0].trim());
        int z1 = Integer.parseInt(parts[1].trim());
        int x2 = Integer.parseInt(parts[2].trim());
        int z2 = Integer.parseInt(parts[3].trim());
        return new TraceRegion(Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2));
    }

    private static TraceCancellationFilter parseCancellation(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "any" -> TraceCancellationFilter.ANY;
            case "yes", "true", "cancelled" -> TraceCancellationFilter.CANCELLED;
            case "no", "false", "non-cancelled", "noncancelled" -> TraceCancellationFilter.NON_CANCELLED;
            default -> throw new IllegalArgumentException("Cancelled filter must be any, yes, or no.");
        };
    }

    private static long parseDurationMillis(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.endsWith("ms")) {
            long millis = Long.parseLong(normalized.substring(0, normalized.length() - 2));
            if (millis <= 0) {
                throw new IllegalArgumentException("Duration must be positive.");
            }
            return millis;
        }
        if (normalized.endsWith("s")) {
            long seconds = Long.parseLong(normalized.substring(0, normalized.length() - 1));
            if (seconds <= 0) {
                throw new IllegalArgumentException("Duration must be positive.");
            }
            return seconds * 1_000L;
        }
        if (normalized.endsWith("m")) {
            long minutes = Long.parseLong(normalized.substring(0, normalized.length() - 1));
            if (minutes <= 0) {
                throw new IllegalArgumentException("Duration must be positive.");
            }
            return minutes * 60_000L;
        }
        long seconds = Long.parseLong(normalized);
        if (seconds <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        return seconds * 1_000L;
    }

    private static long parseThresholdNanos(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.endsWith("ms")) {
            double millis = Double.parseDouble(normalized.substring(0, normalized.length() - 2));
            if (millis <= 0) {
                throw new IllegalArgumentException("Slow threshold must be positive.");
            }
            return Math.round(millis * 1_000_000L);
        }
        if (normalized.endsWith("ns")) {
            double nanos = Double.parseDouble(normalized.substring(0, normalized.length() - 2));
            if (nanos <= 0) {
                throw new IllegalArgumentException("Slow threshold must be positive.");
            }
            return Math.round(nanos);
        }
        if (normalized.endsWith("s")) {
            double seconds = Double.parseDouble(normalized.substring(0, normalized.length() - 1));
            if (seconds <= 0) {
                throw new IllegalArgumentException("Slow threshold must be positive.");
            }
            return Math.round(seconds * 1_000_000_000L);
        }
        double millis = Double.parseDouble(normalized);
        if (millis <= 0) {
            throw new IllegalArgumentException("Slow threshold must be positive.");
        }
        return Math.round(millis * 1_000_000L);
    }
}
