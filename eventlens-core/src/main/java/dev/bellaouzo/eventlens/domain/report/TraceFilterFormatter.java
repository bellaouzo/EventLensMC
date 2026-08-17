package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceRegion;
import java.util.Locale;
import java.util.Optional;

public final class TraceFilterFormatter {

    private TraceFilterFormatter() {}

    public static String describe(TraceFilter filter) {
        StringBuilder builder = new StringBuilder();
        appendOptional(builder, "plugin", filter.pluginName());
        appendOptional(builder, "player", filter.playerName());
        appendOptional(builder, "world", filter.worldName());
        filter.region().ifPresent(region -> appendRegion(builder, region));
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append("cancelled=").append(filter.cancellationFilter().name().toLowerCase(Locale.ROOT));
        return builder.toString();
    }

    private static void appendRegion(StringBuilder builder, TraceRegion region) {
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append("region=")
                .append(region.minX())
                .append(',')
                .append(region.minZ())
                .append(',')
                .append(region.maxX())
                .append(',')
                .append(region.maxZ());
    }

    private static void appendOptional(StringBuilder builder, String label, Optional<String> value) {
        value.ifPresent(actual -> {
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(label).append('=').append(actual);
        });
    }
}
