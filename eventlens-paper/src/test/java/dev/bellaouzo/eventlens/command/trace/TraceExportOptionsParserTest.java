package dev.bellaouzo.eventlens.command.trace;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import org.junit.jupiter.api.Test;

class TraceExportOptionsParserTest {

    @Test
    void rejectsInvalidFormat() {
        TraceExportOptionsParser.Result result =
                TraceExportOptionsParser.parse(new String[] {"export", "abc12345", "--format", "xml"}, 0);
        assertTrue(result.errorMessage().isPresent());
        assertTrue(result.errorMessage().get().contains("Valid types"));
        assertTrue(result.errorMessage().get().contains("xml"));
    }

    @Test
    void acceptsTextAlias() {
        TraceExportOptionsParser.Result result =
                TraceExportOptionsParser.parse(new String[] {"export", "abc12345", "--format", "text"}, 0);
        assertTrue(result.parsed().isPresent());
        assertTrue(result.errorMessage().isEmpty());
    }

    @Test
    void acceptsNdjsonFormat() {
        TraceExportOptionsParser.Result result =
                TraceExportOptionsParser.parse(new String[] {"export", "abc12345", "--format", "ndjson"}, 0);
        assertTrue(result.parsed().isPresent());
        assertTrue(result.errorMessage().isEmpty());
        assertSame(ExportFormat.NDJSON, result.parsed().orElseThrow().format());
    }
}
