package dev.bellaouzo.eventlens.modcommon.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModClientExportParserTest {

    @Test
    void parsesFormatAndFull() {
        ModClientExportParser.Result result =
                ModClientExportParser.parse(List.of("trace", "export", "abc12345", "--format", "html", "--full"));
        assertTrue(result.error().isEmpty());
        assertEquals(ExportFormat.HTML, result.format().orElseThrow());
        assertEquals(ExportRedactionMode.FULL, result.redaction().orElseThrow());
    }

    @Test
    void parsesEqualsFormat() {
        ModClientExportParser.Result result =
                ModClientExportParser.parse(List.of("trace", "export", "abc12345", "--format=ndjson"));
        assertTrue(result.error().isEmpty());
        assertEquals(ExportFormat.NDJSON, result.format().orElseThrow());
    }

    @Test
    void rejectsBundle() {
        ModClientExportParser.Result result =
                ModClientExportParser.parse(List.of("trace", "export", "abc12345", "--format", "bundle"));
        assertTrue(result.error().orElseThrow().contains("bundle"));
    }

    @Test
    void rejectsUnknownArgument() {
        ModClientExportParser.Result result =
                ModClientExportParser.parse(List.of("trace", "export", "abc12345", "--pretty"));
        assertTrue(result.error().orElseThrow().contains("--pretty"));
    }
}
