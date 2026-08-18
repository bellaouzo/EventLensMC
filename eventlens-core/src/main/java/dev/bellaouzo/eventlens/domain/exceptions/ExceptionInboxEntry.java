package dev.bellaouzo.eventlens.domain.exceptions;

import java.util.Optional;

public record ExceptionInboxEntry(
        long capturedAtMillis,
        String sessionId,
        String eventClassName,
        String pluginName,
        String methodName,
        String exceptionType,
        Optional<String> stackTrace) {}
