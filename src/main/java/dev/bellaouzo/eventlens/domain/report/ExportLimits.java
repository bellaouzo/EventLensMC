package dev.bellaouzo.eventlens.domain.report;

public final class ExportLimits {

    public static final int MAX_PENDING_EXPORTS = 2;
    public static final long MAX_EXPORT_FILE_BYTES = 32L * 1024L * 1024L;
    public static final int DEFAULT_RETENTION_DAYS = 30;
    public static final String REPORT_VERSION = "2";

    private ExportLimits() {}
}
