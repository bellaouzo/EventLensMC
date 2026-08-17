package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.command.CommandText;
import java.util.List;

final class TraceBaselineTabCompleter {

    static final String ACTION_SAVE = "save";
    static final String ACTION_LIST = "list";
    static final String ACTION_DELETE = "delete";

    private static final List<String> BASELINE_ACTIONS =
            List.of(ACTION_SAVE, ACTION_LIST, TraceCommandTabCompleter.SUBCOMMAND_COMPARE, ACTION_DELETE);

    private TraceBaselineTabCompleter() {}

    static List<String> complete(TraceCommandService traceCommandService, String[] args, String prefix) {
        if (args.length == 3) {
            return CommandText.filterPrefix(BASELINE_ACTIONS, prefix);
        }
        if (args.length == 4 && args[2].equalsIgnoreCase(ACTION_SAVE)) {
            return CommandText.filterPrefix(traceCommandService.listSessionIds(), prefix);
        }
        if (args.length == 4
                && (args[2].equalsIgnoreCase(TraceCommandTabCompleter.SUBCOMMAND_COMPARE)
                        || args[2].equalsIgnoreCase(ACTION_DELETE))) {
            return List.of();
        }
        if (args.length >= 6 && args[2].equalsIgnoreCase(TraceCommandTabCompleter.SUBCOMMAND_COMPARE)) {
            if (TraceCommandTabCompleter.previousToken(args).equalsIgnoreCase(TraceCommandTabCompleter.PLUGIN_FLAG)) {
                return CommandText.filterPrefix(traceCommandService.listDispatchPluginNames(args[3]), prefix);
            }
            if (prefix.startsWith("-") || args.length == 6) {
                return CommandText.filterPrefix(List.of(TraceCommandTabCompleter.PLUGIN_FLAG), prefix);
            }
        }
        if (args.length >= 6 && args[2].equalsIgnoreCase(ACTION_SAVE)) {
            return CommandText.filterPrefix(
                    List.of(
                            TraceCommandTabCompleter.SHAREABLE_FLAG,
                            TraceCommandTabCompleter.REDACTED_FLAG,
                            TraceCommandTabCompleter.FULL_FLAG),
                    prefix);
        }
        return List.of();
    }
}
