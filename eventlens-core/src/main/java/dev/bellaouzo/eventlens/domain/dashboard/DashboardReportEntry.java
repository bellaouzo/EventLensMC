package dev.bellaouzo.eventlens.domain.dashboard;

import org.jspecify.annotations.NonNull;

public record DashboardReportEntry(
        @NonNull String fileName, long lastModifiedMillis, long sizeBytes, @NonNull String format) {}
