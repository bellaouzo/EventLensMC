package dev.bellaouzo.eventlens.domain.diff;

import org.jspecify.annotations.NonNull;

public record TraceDispatchWarning(@NonNull String code, @NonNull String message) {}
