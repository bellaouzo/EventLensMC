package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.application.TraceLiveOptionsParser;
import dev.bellaouzo.eventlens.command.CommandText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;

final class TraceLiveTabCompleter {

    private TraceLiveTabCompleter() {}

    static List<String> complete(TraceCommandService traceCommandService, String[] args, String prefix) {
        if (args.length == 3) {
            return completeThirdToken(traceCommandService, prefix);
        }
        if (args.length >= 4 && TraceLiveCommandHandler.isControlSubcommand(args[2])) {
            return List.of();
        }
        return completeAfterSessionOrFlags(args, prefix);
    }

    private static List<String> completeThirdToken(TraceCommandService traceCommandService, String prefix) {
        if (prefix.startsWith("-")) {
            return CommandText.filterPrefix(TraceLiveOptionsParser.flagSuggestions(), prefix);
        }
        List<String> options = new ArrayList<>(traceCommandService.listSessionIds());
        options.addAll(TraceLiveCommandHandler.controlSubcommands());
        return CommandText.filterPrefix(options, prefix);
    }

    private static List<String> completeAfterSessionOrFlags(String[] args, String prefix) {
        String previous = previousToken(args);
        if (previous.equalsIgnoreCase("--filter-plugin")) {
            return CommandText.filterPrefix(listLoadedPluginNames(), prefix);
        }
        if (previous.equalsIgnoreCase("--channels")) {
            return CommandText.filterPrefix(TraceLiveOptionsParser.channelSuggestions(), prefix);
        }
        if (previous.equalsIgnoreCase("--display")) {
            return CommandText.filterPrefix(TraceLiveOptionsParser.displaySuggestions(), prefix);
        }
        if (prefix.startsWith("-")) {
            return CommandText.filterPrefix(TraceLiveOptionsParser.flagSuggestions(), prefix);
        }
        if (args.length == 4 && !TraceLiveCommandHandler.isControlSubcommand(args[2])) {
            return CommandText.filterPrefix(TraceLiveOptionsParser.flagSuggestions(), prefix);
        }
        return List.of();
    }

    private static List<String> listLoadedPluginNames() {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(plugin -> Objects.requireNonNullElse(plugin.getName(), ""))
                .filter(name -> !name.isBlank())
                .toList();
    }

    private static String previousToken(String[] args) {
        return args[args.length - 2];
    }
}
