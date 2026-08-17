package dev.bellaouzo.eventlens.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class TraceStartConfirmCommands {

    private TraceStartConfirmCommands() {}

    static Optional<String> hotEventConfirmCommand(
            String eventSimpleName, TraceCommandService.TraceStartOptions options) {
        List<String> parts = new ArrayList<>();
        parts.add("/eventlens trace start " + eventSimpleName);
        options.filter().pluginName().ifPresent(value -> {
            parts.add("--plugin");
            parts.add(value);
        });
        options.filter().playerName().ifPresent(value -> {
            parts.add("--player");
            parts.add(value);
        });
        options.filter().worldName().ifPresent(value -> {
            parts.add("--world");
            parts.add(value);
        });
        options.filter().region().ifPresent(value -> {
            parts.add("--region");
            parts.add(value.minX() + "," + value.minZ() + "," + value.maxX() + "," + value.maxZ());
        });
        options.maxEventCount().ifPresent(value -> {
            parts.add("--max-events");
            parts.add(String.valueOf(value));
        });
        options.maxDurationMillis().ifPresent(value -> {
            parts.add("--max-duration");
            parts.add(value + "ms");
        });
        if (options.slowThresholdNanos() != EventLensCommandConfig.defaults().defaultSlowThresholdNanos()) {
            parts.add("--slow-threshold");
            parts.add(formatThresholdMillis(options.slowThresholdNanos()));
        }
        if (options.captureStacks()) {
            parts.add("--capture-stacks");
        }
        parts.add("--confirm-hot");
        return Optional.of(String.join(" ", parts));
    }

    private static String formatThresholdMillis(long nanos) {
        double millis = nanos / 1_000_000.0;
        if (Math.rint(millis) == millis) {
            return ((long) millis) + "ms";
        }
        return String.format(Locale.ROOT, "%.2fms", millis);
    }
}
