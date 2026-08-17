package dev.bellaouzo.eventlens.modcommon;

import org.jspecify.annotations.NonNull;

public record ModHandlerRegistration(
        @NonNull String modId,
        @NonNull String handlerClassName,
        @NonNull String methodName,
        int priority) {}
