package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.command.CommandText;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;

final class TraceStartTabCompleter {

    private static final List<String> DETAIL_VALUES = List.of("brief", "normal", "verbose");
    private static final List<String> CANCELLED_FILTER_VALUES = List.of("any", "yes", "no");

    private TraceStartTabCompleter() {}

    static List<String> complete(TraceCommandService traceCommandService, String[] args, String prefix) {
        String previous = args[args.length - 2];
        if (previous.equalsIgnoreCase("--player")) {
            return CommandText.filterPrefix(
                    Bukkit.getOnlinePlayers().stream()
                            .map(player -> player == null ? "" : Objects.requireNonNullElse(player.getName(), ""))
                            .toList(),
                    prefix);
        }
        if (previous.equalsIgnoreCase("--plugin")) {
            return CommandText.filterPrefix(
                    Arrays.stream(Bukkit.getPluginManager().getPlugins())
                            .map(plugin -> Objects.requireNonNullElse(plugin.getName(), ""))
                            .toList(),
                    prefix);
        }
        if (previous.equalsIgnoreCase("--world")) {
            return CommandText.filterPrefix(
                    Bukkit.getWorlds().stream()
                            .map(world -> world == null ? "" : Objects.requireNonNullElse(world.getName(), ""))
                            .toList(),
                    prefix);
        }
        if (previous.equalsIgnoreCase("--cancelled")) {
            return CommandText.filterPrefix(CANCELLED_FILTER_VALUES, prefix);
        }
        if (previous.equalsIgnoreCase("--preset")) {
            return CommandText.filterPrefix(traceCommandService.listPresetNames(), prefix);
        }
        if (previous.equalsIgnoreCase("--detail")) {
            return CommandText.filterPrefix(DETAIL_VALUES, prefix);
        }
        if (prefix.startsWith("-")) {
            return CommandText.filterPrefix(
                    List.of(
                            "--preset",
                            "--plugin",
                            "--player",
                            "--world",
                            "--region",
                            "--cancelled",
                            "--max-events",
                            "--max-duration",
                            "--slow-threshold",
                            "--capture-stacks",
                            "--confirm-hot",
                            "--generic",
                            "--detail"),
                    prefix);
        }
        return completeEventQuery(traceCommandService.listSupportedEventSimpleNames(), prefix);
    }

    static List<String> completeEventQuery(List<String> eventNames, String prefix) {
        int comma = prefix.lastIndexOf(',');
        String head = comma < 0 ? "" : prefix.substring(0, comma + 1);
        String fragment = comma < 0 ? prefix : prefix.substring(comma + 1);
        Set<String> selected = new HashSet<>();
        if (comma >= 0) {
            for (String part : head.split(",", -1)) {
                String name = part.trim();
                if (!name.isEmpty()) {
                    selected.add(name.toLowerCase(Locale.ROOT));
                }
            }
        }
        List<String> matches = CommandText.filterPrefix(
                eventNames.stream()
                        .filter(name -> !selected.contains(name.toLowerCase(Locale.ROOT)))
                        .toList(),
                fragment);
        return matches.stream().map(name -> head + name).toList();
    }
}
