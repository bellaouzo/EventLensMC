package dev.bellaouzo.eventlens.domain.snapshot;

public sealed interface SnapshotValue
        permits SnapshotValue.Present, SnapshotValue.Unsupported, SnapshotValue.Truncated {

    record Present(String type, String display) implements SnapshotValue {}

    record Unsupported(String reason) implements SnapshotValue {}

    record Truncated(String display, String reason) implements SnapshotValue {}
}
