package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record PluginDescriptor(
        @NonNull String name,
        @NonNull String version,
        boolean enabled,
        @NonNull List<String> hardDependencies,
        @NonNull List<String> softDependencies,
        @NonNull List<String> loadBefore,
        @NonNull List<String> provides) {}
