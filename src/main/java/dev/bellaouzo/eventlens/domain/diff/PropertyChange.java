package dev.bellaouzo.eventlens.domain.diff;

import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;

public record PropertyChange(String property, SnapshotValue before, SnapshotValue after) {}
