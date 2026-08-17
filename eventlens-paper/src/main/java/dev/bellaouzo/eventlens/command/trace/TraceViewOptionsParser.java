package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.DispatchViewFilter;
import dev.bellaouzo.eventlens.command.DetailLevelParser;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import java.util.Locale;
import java.util.Optional;

final class TraceViewOptionsParser {

    private static final String DETAIL_FLAG = "--detail";
    private static final String DETAIL_PREFIX = DETAIL_FLAG + "=";
    private static final String DISPATCH_FLAG = "--dispatch";
    private static final String DISPATCH_PREFIX = DISPATCH_FLAG + "=";
    private static final String PLUGIN_FLAG = "--plugin";
    private static final String PLUGIN_PREFIX = PLUGIN_FLAG + "=";
    private static final String CHANGED_FLAG = "--changed";
    private static final String SLOW_FLAG = "--slow";
    private static final String CONFLICT_FLAG = "--conflict";
    private static final String UNCHANGED_FLAG = "--unchanged";

    private TraceViewOptionsParser() {}

    @SuppressWarnings({"java:S3776", "java:S6541", "java:S135"})
    static Result parse(String[] args, OutputDetailLevel defaultDetailLevel) {
        int page = 1;
        boolean pageAssigned = false;
        boolean includeUnchanged = false;
        OutputDetailLevel detailLevel = defaultDetailLevel;
        DispatchViewFilter.Builder filterBuilder = new DispatchViewFilter.Builder();

        int index = 3;
        while (index < args.length) {
            String token = args[index];

            if (token.equalsIgnoreCase(UNCHANGED_FLAG)) {
                includeUnchanged = true;
                index++;
                continue;
            }
            if (token.equalsIgnoreCase(CHANGED_FLAG)) {
                filterBuilder.changedOnly(true);
                index++;
                continue;
            }
            if (token.equalsIgnoreCase(SLOW_FLAG)) {
                filterBuilder.slowOnly(true);
                index++;
                continue;
            }
            if (token.equalsIgnoreCase(CONFLICT_FLAG)) {
                filterBuilder.conflictOnly(true);
                index++;
                continue;
            }

            if (token.equalsIgnoreCase(DETAIL_FLAG) || token.startsWith(DETAIL_PREFIX)) {
                ValueReadOutcome detailOutcome = readFlagValue(args, index, DETAIL_FLAG, DETAIL_PREFIX);
                Optional<String> detailError = detailOutcome.errorMessage();
                if (detailError.isPresent()) {
                    return Result.error(detailError.orElseThrow());
                }
                Optional<String> detailValue = detailOutcome.value();
                if (detailValue.isEmpty()) {
                    return Result.error("Missing value for --detail.");
                }
                try {
                    detailLevel = OutputDetailLevel.parse(detailValue.orElseThrow());
                } catch (IllegalArgumentException _) {
                    String validValues = OutputDetailLevel.BRIEF.name().toLowerCase(Locale.ROOT) + ", normal, verbose";
                    return Result.error("Unknown detail level \"" + detailValue.orElseThrow() + "\". Valid values: "
                            + validValues + ".");
                }
                index += 1 + detailOutcome.extraTokensConsumed();
                continue;
            }

            if (token.equalsIgnoreCase(DISPATCH_FLAG) || token.startsWith(DISPATCH_PREFIX)) {
                ValueReadOutcome dispatchOutcome = readFlagValue(args, index, DISPATCH_FLAG, DISPATCH_PREFIX);
                Optional<String> dispatchError = dispatchOutcome.errorMessage();
                if (dispatchError.isPresent()) {
                    return Result.error(dispatchError.orElseThrow());
                }
                Optional<String> dispatchValue = dispatchOutcome.value();
                if (dispatchValue.isEmpty()) {
                    return Result.error("Missing value for --dispatch.");
                }
                String value = dispatchValue.orElseThrow();
                long sequence;
                try {
                    sequence = Long.parseLong(value);
                } catch (NumberFormatException _) {
                    return Result.error("Dispatch must be a positive integer.");
                }
                if (sequence < 1L) {
                    return Result.error("Dispatch must be a positive integer.");
                }
                filterBuilder.dispatchSequence(sequence);
                index += 1 + dispatchOutcome.extraTokensConsumed();
                continue;
            }

            if (token.equalsIgnoreCase(PLUGIN_FLAG) || token.startsWith(PLUGIN_PREFIX)) {
                ValueReadOutcome pluginOutcome = readFlagValue(args, index, PLUGIN_FLAG, PLUGIN_PREFIX);
                Optional<String> pluginError = pluginOutcome.errorMessage();
                if (pluginError.isPresent()) {
                    return Result.error(pluginError.orElseThrow());
                }
                Optional<String> pluginValue = pluginOutcome.value();
                if (pluginValue.isEmpty()) {
                    return Result.error("Missing value for --plugin.");
                }
                String pluginName = pluginValue.orElseThrow().trim();
                if (pluginName.isEmpty()) {
                    return Result.error("Plugin name cannot be blank.");
                }
                filterBuilder.pluginName(pluginName);
                index += 1 + pluginOutcome.extraTokensConsumed();
                continue;
            }

            if (token.startsWith("--")) {
                return Result.error(
                        "Unknown view option \"" + token
                                + "\". Valid options: --unchanged, --detail, --dispatch, --plugin, --changed, --slow, --conflict.");
            }

            if (!pageAssigned) {
                try {
                    page = DetailLevelParser.parsePage(token);
                } catch (IllegalArgumentException _) {
                    return Result.error("Page must be a positive integer.");
                }
                pageAssigned = true;
                index++;
                continue;
            }

            return Result.error("Unknown view option \"" + token + "\".");
        }

        return Result.ok(new Parsed(page, detailLevel, includeUnchanged, filterBuilder.build()));
    }

    private static ValueReadOutcome readFlagValue(String[] args, int index, String flag, String prefix) {
        String token = args[index];
        if (token.startsWith(prefix)) {
            return ValueReadOutcome.withValue(token.substring(prefix.length()), 0);
        }
        if (index + 1 >= args.length) {
            return ValueReadOutcome.error("Missing value for " + flag + ".");
        }
        return ValueReadOutcome.withValue(args[index + 1], 1);
    }

    record Parsed(int page, OutputDetailLevel detailLevel, boolean includeUnchanged, DispatchViewFilter filter) {}

    record Result(Optional<Parsed> parsed, Optional<String> errorMessage) {
        static Result ok(Parsed parsed) {
            return new Result(Optional.of(parsed), Optional.empty());
        }

        static Result error(String message) {
            return new Result(Optional.empty(), Optional.of(message));
        }
    }

    private record ValueReadOutcome(Optional<String> value, int extraTokensConsumed, Optional<String> errorMessage) {
        static ValueReadOutcome withValue(String value, int extraTokensConsumed) {
            return new ValueReadOutcome(Optional.of(value), extraTokensConsumed, Optional.empty());
        }

        static ValueReadOutcome error(String message) {
            return new ValueReadOutcome(Optional.empty(), 0, Optional.of(message));
        }
    }
}
