package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class ModClientExportParser {

    private static final String FORMAT_FLAG = "--format";
    private static final String FORMAT_PREFIX = FORMAT_FLAG + "=";

    private ModClientExportParser() {}

    static Result parse(List<String> args) {
        ExportFormat format = ExportFormat.JSON;
        ExportRedactionMode redaction = ExportRedactionMode.SHARE_SAFE;
        int index = 3;
        while (index < args.size()) {
            String token = args.get(index);
            if ("--shareable".equalsIgnoreCase(token) || "--redacted".equalsIgnoreCase(token)) {
                redaction = ExportRedactionMode.SHARE_SAFE;
            } else if ("--full".equalsIgnoreCase(token)) {
                redaction = ExportRedactionMode.FULL;
            } else if (token.toLowerCase(Locale.ROOT).startsWith(FORMAT_PREFIX)) {
                Optional<ExportFormat> parsed = parseFormat(token.substring(FORMAT_PREFIX.length()));
                if (parsed.isEmpty()) {
                    return Result.error(unknownFormat(token.substring(FORMAT_PREFIX.length())));
                }
                format = parsed.orElseThrow();
            } else if (FORMAT_FLAG.equalsIgnoreCase(token)) {
                if (index + 1 >= args.size()) {
                    return Result.error("Missing value for --format. Valid types: json, ndjson, text, html.");
                }
                Optional<ExportFormat> parsed = parseFormat(args.get(++index));
                if (parsed.isEmpty()) {
                    return Result.error(unknownFormat(args.get(index)));
                }
                format = parsed.orElseThrow();
            } else {
                return Result.error(
                        "Unknown export argument \"" + token + "\". Use --format json|ndjson|text|html, --shareable, or --full.");
            }
            index++;
        }
        if (format == ExportFormat.BUNDLE) {
            return Result.error("Client export does not support --format bundle. Use json, ndjson, text, or html.");
        }
        return Result.ok(format, redaction);
    }

    private static Optional<ExportFormat> parseFormat(String value) {
        return ExportFormat.parse(value);
    }

    private static String unknownFormat(String value) {
        return "Unknown export format \"" + value + "\". Valid types: json, ndjson, text, html.";
    }

    record Result(Optional<ExportFormat> format, Optional<ExportRedactionMode> redaction, Optional<String> error) {
        static Result ok(ExportFormat format, ExportRedactionMode redaction) {
            return new Result(Optional.of(format), Optional.of(redaction), Optional.empty());
        }

        static Result error(String message) {
            return new Result(Optional.empty(), Optional.empty(), Optional.of(message));
        }
    }
}
