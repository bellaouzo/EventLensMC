package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

public final class ReportFormatting {

    private static final DateTimeFormatter GENERATED_AT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    private ReportFormatting() {}

    public static String formatGeneratedAt(long millis) {
        return GENERATED_AT.format(Instant.ofEpochMilli(millis));
    }

    public static String formatPartialReasons(Set<TracePartialReason> reasons) {
        return reasons.stream().map(TraceReportWarnings::describePartialReason).collect(Collectors.joining("; "));
    }
}
