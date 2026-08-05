package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import java.util.Optional;

final class TraceExportOptionsParser {

    private static final String FORMAT_FLAG = "--format";
    private static final String FORMAT_PREFIX = FORMAT_FLAG + "=";

    private TraceExportOptionsParser() {}

    static Result parse(String[] args, int flagStartIndex) {
        ExportFormat format = ExportFormat.JSON;
        ExportRedactionMode redactionMode = ExportRedactionMode.FULL;
        int index = flagStartIndex;
        while (index < args.length) {
            String token = args[index];
            if (token.equalsIgnoreCase("--shareable") || token.equalsIgnoreCase("--redacted")) {
                redactionMode = ExportRedactionMode.SHARE_SAFE;
            } else if (token.equalsIgnoreCase("--full")) {
                redactionMode = ExportRedactionMode.FULL;
            } else {
                FormatTokenOutcome outcome = readFormatToken(args, index);
                Optional<String> error = outcome.error();
                if (error.isPresent()) {
                    return Result.error(error.get());
                }
                Optional<ExportFormat> parsedFormat = outcome.format();
                if (parsedFormat.isPresent()) {
                    format = parsedFormat.get();
                    index += outcome.extraTokens();
                }
            }
            index++;
        }
        return Result.ok(new Parsed(format, redactionMode));
    }

    private static FormatTokenOutcome readFormatToken(String[] args, int index) {
        String token = args[index];
        if (token.startsWith(FORMAT_PREFIX)) {
            return parseFormatValue(token.substring(FORMAT_PREFIX.length()), 0);
        }
        if (token.equalsIgnoreCase(FORMAT_FLAG)) {
            if (index + 1 >= args.length) {
                return FormatTokenOutcome.fail(
                        "Missing value for --format. Valid types: " + ExportFormat.validTypesDescription());
            }
            return parseFormatValue(args[index + 1], 1);
        }
        return FormatTokenOutcome.none();
    }

    private static FormatTokenOutcome parseFormatValue(String value, int extraTokens) {
        Optional<ExportFormat> parsed = ExportFormat.parse(value);
        if (parsed.isEmpty()) {
            return FormatTokenOutcome.fail(invalidFormatMessage(value));
        }
        return FormatTokenOutcome.ok(parsed.get(), extraTokens);
    }

    private static String invalidFormatMessage(String value) {
        return "Unknown export format \"" + value + "\". Valid types: " + ExportFormat.validTypesDescription() + ".";
    }

    private record FormatTokenOutcome(Optional<ExportFormat> format, int extraTokens, Optional<String> error) {

        static FormatTokenOutcome none() {
            return new FormatTokenOutcome(Optional.empty(), 0, Optional.empty());
        }

        static FormatTokenOutcome ok(ExportFormat format, int extraTokens) {
            return new FormatTokenOutcome(Optional.of(format), extraTokens, Optional.empty());
        }

        static FormatTokenOutcome fail(String message) {
            return new FormatTokenOutcome(Optional.empty(), 0, Optional.of(message));
        }
    }

    record Parsed(ExportFormat format, ExportRedactionMode redactionMode) {}

    record Result(Optional<Parsed> parsed, Optional<String> errorMessage) {

        static Result ok(Parsed parsed) {
            return new Result(Optional.of(parsed), Optional.empty());
        }

        static Result error(String message) {
            return new Result(Optional.empty(), Optional.of(message));
        }
    }
}
