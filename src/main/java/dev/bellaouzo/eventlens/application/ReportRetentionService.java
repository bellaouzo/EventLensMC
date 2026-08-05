package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.ExportPort;

public final class ReportRetentionService {

    private final ExportPort exportPort;
    private final EventLensReportConfig config;

    public ReportRetentionService(ExportPort exportPort, EventLensReportConfig config) {
        this.exportPort = exportPort;
        this.config = config;
    }

    public int cleanupIfEnabled() {
        if (!config.autoCleanup()) {
            return 0;
        }
        return cleanupNow();
    }

    public int cleanupNow() {
        long retentionMillis = config.retentionDays() * 24L * 60L * 60L * 1000L;
        long cutoff = System.currentTimeMillis() - retentionMillis;
        return exportPort.deleteReportsOlderThan(cutoff);
    }
}
