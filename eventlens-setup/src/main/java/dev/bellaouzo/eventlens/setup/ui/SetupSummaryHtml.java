package dev.bellaouzo.eventlens.setup.ui;

import java.util.List;

final class SetupSummaryHtml {

    private SetupSummaryHtml() {}

    static String render(List<String> lines) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Segoe UI,sans-serif;font-size:12px;margin:0'>");
        for (String line : lines) {
            if (line.isBlank()) {
                html.append("<div style='height:10px'></div>");
                continue;
            }
            html.append("<div style='color:")
                    .append(color(line))
                    .append(";margin:3px 0'>")
                    .append(marker(line))
                    .append(' ')
                    .append(escape(line))
                    .append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    static String color(String line) {
        if (line.startsWith("Copied ")) {
            return "#18753C";
        }
        if (line.startsWith("Updated ")) {
            return "#1A5FA8";
        }
        if (line.startsWith("Skipped ")) {
            return "#8A5A00";
        }
        if (line.startsWith("Next:") || line.startsWith("Then ") || line.startsWith("If ")) {
            return "#3D4654";
        }
        return "#222222";
    }

    static String marker(String line) {
        if (line.startsWith("Copied ") || line.startsWith("Updated ")) {
            return "✓";
        }
        if (line.startsWith("Skipped ")) {
            return "–";
        }
        if (line.startsWith("Next:") || line.startsWith("Then ") || line.startsWith("If ")) {
            return "→";
        }
        return "•";
    }

    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
