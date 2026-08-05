package dev.bellaouzo.eventlens.domain.diff;

import java.util.List;
import java.util.Optional;

public record SnapshotDiff(
        List<PropertyChange> changed,
        List<PropertyChange> unchanged,
        Optional<CancellationTransition> cancellationTransition) {}
