package dev.bellaouzo.eventlens.paper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PaperBundleExporterTest {

    @Test
    void writesSelfContainedIndexAndPrettyReport(@TempDir Path tempDir) throws Exception {
        String json = "{\n  \"session\": {\n    \"sessionId\": \"abc\"\n  }\n}\n";

        var result = PaperBundleExporter.write(tempDir, "trace-test", json);

        assertTrue(result.success());
        Path bundle = tempDir.resolve("trace-test-bundle");
        String report = Files.readString(bundle.resolve("report.json"), StandardCharsets.UTF_8);
        String html = Files.readString(bundle.resolve("index.html"), StandardCharsets.UTF_8);

        assertTrue(report.contains("\n  "));
        assertTrue(html.contains("window.__EVENTLENS_REPORT__"));
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("<div id=\"app\">"));
        assertFalse(Files.exists(bundle.resolve("report.js")));
        assertFalse(Files.isDirectory(bundle.resolve("assets")));
    }

    @Test
    void embedsEventGraphWhenProvided(@TempDir Path tempDir) throws Exception {
        String graph =
                "{\"title\":\"Event relationship graph\",\"nodes\":[{\"id\":\"e1\"}],\"edges\":[],\"truncated\":false}";
        var result = PaperBundleExporter.write(tempDir, "trace-graph", "{}", graph);

        assertTrue(result.success());
        String html =
                Files.readString(tempDir.resolve("trace-graph-bundle").resolve("index.html"), StandardCharsets.UTF_8);
        assertTrue(html.contains("window.__EVENTLENS_EVENT_GRAPH__"));
        assertTrue(html.contains("Event relationship graph"));
    }
}
