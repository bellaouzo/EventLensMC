package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record PluginAttributedChange(
        @NonNull String sessionId,
        @NonNull String eventClassName,
        long dispatchSequence,
        @NonNull String priorityBand,
        @NonNull List<String> changedProperties,
        boolean conflictingAttribution) {}
