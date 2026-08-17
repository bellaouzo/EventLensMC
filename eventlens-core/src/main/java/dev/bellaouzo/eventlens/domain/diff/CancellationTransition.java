package dev.bellaouzo.eventlens.domain.diff;

import org.jspecify.annotations.NonNull;

public record CancellationTransition(boolean before, boolean after, @NonNull CancellationTransitionKind kind) {}
