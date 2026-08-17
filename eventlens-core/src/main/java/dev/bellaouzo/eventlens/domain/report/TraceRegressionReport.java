package dev.bellaouzo.eventlens.domain.report;

import java.util.List;

public record TraceRegressionReport(
        String leftSourceId,
        String rightSourceId,
        String scopeLabel,
        boolean sameEventClass,
        List<String> differences,
        List<String> notes) {

    public TraceRegressionReport {
        leftSourceId = leftSourceId == null ? "left" : leftSourceId;
        rightSourceId = rightSourceId == null ? "right" : rightSourceId;
        scopeLabel = scopeLabel == null || scopeLabel.isBlank() ? "all dispatches" : scopeLabel;
        differences = differences == null ? List.of() : List.copyOf(differences);
        notes = notes == null ? List.of() : List.copyOf(notes);
    }

    public String summaryText() {
        StringBuilder text = new StringBuilder();
        text.append("Compare ").append(leftSourceId).append(" vs ").append(rightSourceId);
        text.append(" (").append(scopeLabel).append(")\n");
        if (differences.isEmpty()) {
            text.append("No regression-significant differences detected.");
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
