package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.command.CommandLiterals;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import java.util.List;
import java.util.Locale;

final class TraceCommandTabCompleter {

    static final String SUBCOMMAND_START = "start";
    static final String SUBCOMMAND_LIVE = "live";
    private static final String SUBCOMMAND_EXPORT = CommandLiterals.SUBCOMMAND_EXPORT;
    private static final String SUBCOMMAND_COPY = "copy";
    static final String SUBCOMMAND_COMPARE = "compare";
    private static final String SUBCOMMAND_BASELINE = "baseline";
    private static final String SUBCOMMAND_VIEW = "view";
    private static final String SUBCOMMAND_FAVORITE = CommandLiterals.SUBCOMMAND_FAVORITE;
    private static final String DISPATCH_FLAG = "--dispatch";
    static final String PLUGIN_FLAG = "--plugin";
    static final String SHAREABLE_FLAG = "--shareable";
    static final String REDACTED_FLAG = "--redacted";
    static final String FULL_FLAG = "--full";
    private static final String UNCHANGED_FLAG = "--unchanged";
    private static final String DETAIL_FLAG = "--detail";
    private static final String CHANGED_FLAG = "--changed";
    private static final String SLOW_FLAG = "--slow";
    private static final String CONFLICT_FLAG = "--conflict";

    private static final List<String> TRACE_SUBCOMMANDS = List.of(
            SUBCOMMAND_START,
            "stop",
            "list",
            SUBCOMMAND_VIEW,
            SUBCOMMAND_EXPORT,
            SUBCOMMAND_COPY,
            SUBCOMMAND_COMPARE,
            SUBCOMMAND_BASELINE,
            "history",
            SUBCOMMAND_FAVORITE,
            "presets",
            SUBCOMMAND_LIVE);
    private static final List<String> FAVORITE_SUBCOMMANDS = List.of("list", "add", "remove");
    private static final List<String> SESSION_SUBCOMMANDS =
            List.of(SUBCOMMAND_VIEW, SUBCOMMAND_EXPORT, SUBCOMMAND_COPY);
    private static final List<String> EXPORT_FLAGS = List.of("--format", SHAREABLE_FLAG, REDACTED_FLAG, FULL_FLAG);
    private static final List<String> COPY_FLAGS = List.of(SHAREABLE_FLAG, REDACTED_FLAG, FULL_FLAG, DISPATCH_FLAG);
    private static final List<String> COMPARE_FLAGS = List.of(PLUGIN_FLAG, SHAREABLE_FLAG, REDACTED_FLAG, FULL_FLAG);
    private static final List<String> TRACE_VIEW_FLAGS =
            List.of(UNCHANGED_FLAG, DETAIL_FLAG, DISPATCH_FLAG, PLUGIN_FLAG, CHANGED_FLAG, SLOW_FLAG, CONFLICT_FLAG);
    private static final List<String> DETAIL_VALUES =
            List.of(OutputDetailLevel.BRIEF.name().toLowerCase(Locale.ROOT), "normal", "verbose");

    private TraceCommandTabCompleter() {}

    @SuppressWarnings("java:S3776")
    static List<String> complete(TraceCommandService traceCommandService, String[] args, String prefix) {
        if (args.length == 2) {
            return completeSecondToken(traceCommandService, args, prefix);
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase(SUBCOMMAND_LIVE)) {
            return TraceLiveTabCompleter.complete(traceCommandService, args, prefix);
        }
        if (args.length == 3) {
            return completeThirdToken(traceCommandService, args, prefix);
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase(SUBCOMMAND_COMPARE)) {
            return completeCompare(traceCommandService, args, prefix);
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase(SUBCOMMAND_BASELINE)) {
            return TraceBaselineTabCompleter.complete(traceCommandService, args, prefix);
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase(SUBCOMMAND_VIEW)) {
            return completeView(traceCommandService, args, prefix);
        }
        if (args.length >= 4 && usesExportFlags(args[1])) {
            return completeExportFlags(traceCommandService, args, prefix, 3);
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase(SUBCOMMAND_START)) {
            return TraceStartTabCompleter.complete(traceCommandService, args, prefix);
        }
        if (args.length >= 4
                && args[1].equalsIgnoreCase(SUBCOMMAND_FAVORITE)
                && (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove"))) {
            return CommandText.filterPrefix(traceCommandService.listSupportedEventSimpleNames(), prefix);
        }
        return List.of();
    }

    private static List<String> completeSecondToken(
            TraceCommandService traceCommandService, String[] args, String prefix) {
        if (args[1].equalsIgnoreCase(SUBCOMMAND_START)) {
            return CommandText.filterPrefix(traceCommandService.listSupportedEventSimpleNames(), prefix);
        }
        return CommandText.filterPrefix(TRACE_SUBCOMMANDS, prefix);
    }

    private static List<String> completeThirdToken(
            TraceCommandService traceCommandService, String[] args, String prefix) {
        if (args[1].equalsIgnoreCase(SUBCOMMAND_START)) {
            return CommandText.filterPrefix(traceCommandService.listSupportedEventSimpleNames(), prefix);
        }
        if (args[1].equalsIgnoreCase(SUBCOMMAND_FAVORITE)) {
            return CommandText.filterPrefix(FAVORITE_SUBCOMMANDS, prefix);
        }
        if (isSessionSubcommand(args[1]) || args[1].equalsIgnoreCase(SUBCOMMAND_COMPARE)) {
            return CommandText.filterPrefix(traceCommandService.listSessionIds(), prefix);
        }
        return List.of();
    }

    private static List<String> completeView(TraceCommandService traceCommandService, String[] args, String prefix) {
        String sessionId = args[2];
        String previous = previousToken(args);
        if (previous.equalsIgnoreCase(DETAIL_FLAG)) {
            return CommandText.filterPrefix(DETAIL_VALUES, prefix);
        }
        if (previous.equalsIgnoreCase(DISPATCH_FLAG)) {
            return CommandText.filterPrefix(traceCommandService.listDispatchSequenceTokens(sessionId), prefix);
        }
        if (previous.equalsIgnoreCase(PLUGIN_FLAG)) {
            return CommandText.filterPrefix(traceCommandService.listDispatchPluginNames(sessionId), prefix);
        }
        if (prefix.startsWith("-")) {
            return CommandText.filterPrefix(TRACE_VIEW_FLAGS, prefix);
        }
        if (args.length == 4) {
            return CommandText.filterPrefix(TRACE_VIEW_FLAGS, prefix);
        }
        return List.of();
    }

    private static List<String> completeCompare(TraceCommandService traceCommandService, String[] args, String prefix) {
        if (args.length == 3 || args.length == 4) {
            return CommandText.filterPrefix(traceCommandService.listSessionIds(), prefix);
        }
        String previous = previousToken(args);
        if (previous.equalsIgnoreCase(PLUGIN_FLAG)) {
            return CommandText.filterPrefix(traceCommandService.listDispatchPluginNames(args[2]), prefix);
        }
        if (prefix.startsWith("-")) {
            return CommandText.filterPrefix(COMPARE_FLAGS, prefix);
        }
        if (args.length == 5) {
            return CommandText.filterPrefix(COMPARE_FLAGS, prefix);
        }
        return List.of();
    }

    private static boolean isSessionSubcommand(String subcommand) {
        return SESSION_SUBCOMMANDS.stream().anyMatch(name -> name.equalsIgnoreCase(subcommand));
    }

    private static boolean usesExportFlags(String subcommand) {
        return SUBCOMMAND_EXPORT.equalsIgnoreCase(subcommand) || SUBCOMMAND_COPY.equalsIgnoreCase(subcommand);
    }

    private static List<String> completeExportFlags(
            TraceCommandService traceCommandService, String[] args, String prefix, int flagStartIndex) {
        String previous = previousToken(args);
        if (previous.equalsIgnoreCase("--format")) {
            return CommandText.filterPrefix(ExportFormat.tabCompletionValues(), prefix);
        }
        if (previous.equalsIgnoreCase(DISPATCH_FLAG) && args[1].equalsIgnoreCase(SUBCOMMAND_COPY)) {
            return CommandText.filterPrefix(traceCommandService.listDispatchSequenceTokens(args[2]), prefix);
        }
        if (prefix.startsWith("-")) {
            List<String> flags = args[1].equalsIgnoreCase(SUBCOMMAND_EXPORT) ? EXPORT_FLAGS : COPY_FLAGS;
            return CommandText.filterPrefix(flags, prefix);
        }
        if (args.length == flagStartIndex + 1) {
            List<String> flags = args[1].equalsIgnoreCase(SUBCOMMAND_EXPORT) ? EXPORT_FLAGS : COPY_FLAGS;
            return CommandText.filterPrefix(flags, prefix);
        }
        return List.of();
    }

    static String previousToken(String[] args) {
        return args[args.length - 2];
    }
}
