package dev.bellaouzo.eventlens.domain.report;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record TraceReportComparison(
        @NonNull String leftSessionId,
        @NonNull String rightSessionId,
        boolean sameEventClass,
        @NonNull List<String> differences,
        @NonNull List<String> notes) {

    public TraceReportComparison {
        differences = differences == null ? List.of() : List.copyOf(differences);
        notes = notes == null ? List.of() : List.copyOf(notes);
    }

    public String summaryText() {
        StringBuilder text = new StringBuilder();
        text.append("Compare ")
                .append(leftSessionId)
                .append(" vs ")
                .append(rightSessionId)
                .append('\n');
        if (differences.isEmpty()) {
            text.append("No major differences detected.");
        } else {
            for (String difference : differences) {
                text.append("- ").append(difference).append('\n');
            }
        }
        for (String note : notes) {
            text.append("Note: ").append(note).append('\n');
        }
        return text.toString().trim();
    }
}
