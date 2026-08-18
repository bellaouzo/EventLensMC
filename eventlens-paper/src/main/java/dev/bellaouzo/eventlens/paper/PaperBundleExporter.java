package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.domain.report.TraceReportJsonSupport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class PaperBundleExporter {

    private static final String DASHBOARD_PREFIX = "dashboard/";
    private static final String INDEX_HTML = "index.html";
    private static final String STYLE_CSS = "assets/style.css";
    private static final String INDEX_JS = "assets/index.js";

    private PaperBundleExporter() {}

    static ExportPort.ExportWriteResult write(Path reportsDirectory, String safeBaseName, String reportJson) {
        try {
            Files.createDirectories(reportsDirectory);
            Path target = reportsDirectory.resolve(safeBaseName + "-bundle").normalize();
            if (!target.startsWith(reportsDirectory)) {
                return ExportPort.ExportWriteResult.failure("Invalid export path.");
            }
            Files.createDirectories(target);
            Files.writeString(target.resolve("report.json"), reportJson, StandardCharsets.UTF_8);
            String css = readDashboardResource(STYLE_CSS);
            String js = readDashboardResource(INDEX_JS);
            if (css.isBlank() || js.isBlank()) {
                writeFallback(target);
            } else {
                Files.writeString(target.resolve(INDEX_HTML), buildIndex(css, reportJson, js), StandardCharsets.UTF_8);
            }
            return ExportPort.ExportWriteResult.success(target);
        } catch (IOException ex) {
            return ExportPort.ExportWriteResult.failure(
                    ex.getMessage() == null ? "I/O error writing bundle." : ex.getMessage());
        }
    }

    private static String buildIndex(String css, String reportJson, String js) {
        String payload = escapeEmbedded(TraceReportJsonSupport.minifyJson(reportJson));
        return "<!doctype html>\n"
                + "<html lang=\"en\">\n"
                + "  <head>\n"
                + "    <meta charset=\"UTF-8\" />\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
                + "    <title>EventLens Diagnostics</title>\n"
                + "    <style>\n"
                + css
                + "\n    </style>\n"
                + "    <script>window.__EVENTLENS_REPORT__="
                + payload
                + ";</script>\n"
                + "  </head>\n"
                + "  <body>\n"
                + "    <div id=\"app\"></div>\n"
                + "    <script>\n"
                + escapeEmbedded(js)
                + "\n    </script>\n"
                + "  </body>\n"
                + "</html>\n";
    }

    private static String readDashboardResource(String relativePath) throws IOException {
        String name = DASHBOARD_PREFIX + relativePath;
        try (InputStream input = PaperBundleExporter.class.getClassLoader().getResourceAsStream(name)) {
            if (input == null) {
                return "";
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String escapeEmbedded(String value) {
        return value.replace("</", "<\\/");
    }

    private static void writeFallback(Path target) throws IOException {
        Files.writeString(
                target.resolve(INDEX_HTML),
                "<!doctype html><meta charset=utf-8><title>EventLens</title>"
                        + "<p>Open report.json in the EventLens viewer.</p>",
                StandardCharsets.UTF_8);
    }
}
