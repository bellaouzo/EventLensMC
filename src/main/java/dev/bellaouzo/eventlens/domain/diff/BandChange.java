package dev.bellaouzo.eventlens.domain.diff;

import java.util.List;

public record BandChange(
        String priorityBand, List<String> attributedPlugins, boolean conflictingAttribution, SnapshotDiff diff) {}
