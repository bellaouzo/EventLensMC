package dev.bellaouzo.eventlens.command.plugin;

import dev.bellaouzo.eventlens.application.PluginQueryService;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class PluginCommandTabCompleter {

    private static final String SUBCOMMAND_COMPARE = "compare";
    private static final String SUBCOMMAND_LISTENERS = "listeners";
    private static final String DETAIL_FLAG = "--detail";
    private static final List<String> PROFILE_FLAGS = List.of(DETAIL_FLAG);
    private static final List<String> DETAIL_VALUES =
            List.of(OutputDetailLevel.BRIEF.name().toLowerCase(Locale.ROOT), "normal", "verbose");

    private PluginCommandTabCompleter() {}

    static List<String> complete(PluginQueryService pluginQueryService, String[] args, String prefix) {
        if (args.length == 2) {
            return completePluginName(pluginQueryService, prefix);
        }
        if (args[1].equalsIgnoreCase(SUBCOMMAND_COMPARE)) {
            return completeCompare(pluginQueryService, args, prefix);
        }
        if (isListenersPath(args)) {
            return completeListeners(pluginQueryService, args, prefix);
        }
        return completeProfile(args, prefix);
    }

    private static List<String> completePluginName(PluginQueryService pluginQueryService, String prefix) {
        List<String> options = new ArrayList<>(pluginQueryService.listPluginNames());
        options.add(SUBCOMMAND_COMPARE);
        return CommandText.filterPrefix(options, prefix);
    }

    private static List<String> completeCompare(PluginQueryService pluginQueryService, String[] args, String prefix) {
        if (args.length == 3 || args.length == 4) {
            return CommandText.filterPrefix(pluginQueryService.listPluginNames(), prefix);
        }
        return List.of();
    }

    private static List<String> completeProfile(String[] args, String prefix) {
        if (args.length == 3) {
            if (prefix.startsWith("-")) {
                return CommandText.filterPrefix(PROFILE_FLAGS, prefix);
            }
            List<String> options = new ArrayList<>();
            options.add(SUBCOMMAND_LISTENERS);
            options.addAll(PROFILE_FLAGS);
            return CommandText.filterPrefix(options, prefix);
        }
        if (previousToken(args).equalsIgnoreCase(DETAIL_FLAG)) {
            return CommandText.filterPrefix(DETAIL_VALUES, prefix);
        }
        return List.of();
    }

    private static List<String> completeListeners(PluginQueryService pluginQueryService, String[] args, String prefix) {
        if (args.length == 3) {
            return CommandText.filterPrefix(List.of(SUBCOMMAND_LISTENERS), prefix);
        }
        if (args.length == 4) {
            if (prefix.startsWith("-")) {
                return CommandText.filterPrefix(PROFILE_FLAGS, prefix);
            }
            return completeListenerEventSuggestions(pluginQueryService, args[1], prefix);
        }
        if (previousToken(args).equalsIgnoreCase(DETAIL_FLAG)) {
            return CommandText.filterPrefix(DETAIL_VALUES, prefix);
        }
        if (prefix.startsWith("-")) {
            return CommandText.filterPrefix(PROFILE_FLAGS, prefix);
        }
        return List.of();
    }

    private static List<String> completeListenerEventSuggestions(
            PluginQueryService pluginQueryService, String pluginQuery, String prefix) {
        Set<String> merged = new LinkedHashSet<>();
        pluginQueryService
                .resolvePluginName(pluginQuery)
                .ifPresent(resolved -> merged.addAll(pluginQueryService.listEventSimpleNamesForPlugin(resolved)));
        merged.addAll(pluginQueryService.listKnownEventSimpleNames());
        List<String> sorted =
                merged.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
        return CommandText.filterPrefix(sorted, prefix);
    }

    private static boolean isListenersPath(String[] args) {
        if (args.length < 3) {
            return false;
        }
        String token = args[2];
        if (token.equalsIgnoreCase(SUBCOMMAND_LISTENERS)) {
            return true;
        }
        return args.length == 3
                && SUBCOMMAND_LISTENERS.toLowerCase(Locale.ROOT).startsWith(token.toLowerCase(Locale.ROOT));
    }

    private static String previousToken(String[] args) {
        return args.length >= 2 ? args[args.length - 2] : "";
    }
}
