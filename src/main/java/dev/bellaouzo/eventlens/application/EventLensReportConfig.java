package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.report.ExportLimits;

public record EventLensReportConfig(int retentionDays, boolean autoCleanup) {

    public EventLensReportConfig {
        if (retentionDays < 1) {
            retentionDays = ExportLimits.DEFAULT_RETENTION_DAYS;
        }
    }

    public static EventLensReportConfig defaults() {
        return new EventLensReportConfig(ExportLimits.DEFAULT_RETENTION_DAYS, true);
    }
}
