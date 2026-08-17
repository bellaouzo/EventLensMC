package dev.bellaouzo.eventlens.domain.trace;

import java.util.Locale;
import java.util.Optional;

public record TraceFilter(
        Optional<String> pluginName,
        Optional<String> playerName,
        Optional<String> worldName,
        Optional<TraceRegion> region,
        TraceCancellationFilter cancellationFilter) {

    public static TraceFilter unrestricted() {
        return new TraceFilter(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), TraceCancellationFilter.ANY);
    }

    public static final class Builder {

        private Optional<String> pluginName = Optional.empty();
        private Optional<String> playerName = Optional.empty();
        private Optional<String> worldName = Optional.empty();
        private Optional<TraceRegion> region = Optional.empty();
        private TraceCancellationFilter cancellationFilter = TraceCancellationFilter.ANY;

        public static Builder unrestricted() {
            return new Builder();
        }

        public Builder pluginName(String value) {
            pluginName = Optional.of(value);
            return this;
        }

        public Builder playerName(String value) {
            playerName = Optional.of(value);
            return this;
        }

        public Builder worldName(String value) {
            worldName = Optional.of(value);
            return this;
        }

        public Builder region(TraceRegion value) {
            region = Optional.of(value);
            return this;
        }

        public Builder cancellationFilter(TraceCancellationFilter value) {
            cancellationFilter = value;
            return this;
        }

        public TraceFilter build() {
            return new TraceFilter(pluginName, playerName, worldName, region, cancellationFilter);
        }
    }

    public boolean matches(EventFilterContext context) {
        return matchesPluginFilter(context)
                && matchesPlayerFilter(context)
                && matchesWorldFilter(context)
                && matchesRegionFilter(context)
                && matchesCancellationFilter(context);
    }

    private boolean matchesPluginFilter(EventFilterContext context) {
        if (pluginName.isEmpty()) {
            return true;
        }
        String required = pluginName.get().toLowerCase(Locale.ROOT);
        return context.listenerPluginNames().stream()
                .anyMatch(name -> name.toLowerCase(Locale.ROOT).equals(required));
    }

    private boolean matchesPlayerFilter(EventFilterContext context) {
        if (playerName.isEmpty()) {
            return true;
        }
        return context.playerName()
                .map(name -> name.equalsIgnoreCase(playerName.get()))
                .orElse(false);
    }

    private boolean matchesWorldFilter(EventFilterContext context) {
        if (worldName.isEmpty()) {
            return true;
        }
        return context.worldName()
                .map(name -> name.equalsIgnoreCase(worldName.get()))
                .orElse(false);
    }

    private boolean matchesRegionFilter(EventFilterContext context) {
        if (region.isEmpty()) {
            return true;
        }
        return region.flatMap(selectedRegion ->
                        context.blockX().flatMap(x -> context.blockZ().map(z -> selectedRegion.contains(x, z))))
                .orElse(false);
    }

    private boolean matchesCancellationFilter(EventFilterContext context) {
        return switch (cancellationFilter) {
            case ANY -> true;
            case CANCELLED -> context.cancellable() && context.cancelled();
            case NON_CANCELLED -> !context.cancellable() || !context.cancelled();
        };
    }
}
