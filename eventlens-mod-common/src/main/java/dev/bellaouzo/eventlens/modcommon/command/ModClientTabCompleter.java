package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ModClientTabCompleter {

    private static final List<String> ROOT = List.of("status", "listeners", "trace", "mod", "exceptions", "ui");
    private static final List<String> TRACE =
            List.of("start", "stop", "pause", "resume", "restart", "list", "view", "export", "live");
    private ModClientTabCompleter() {}

    public static List<String> complete(ModTraceCoordinator coordinator, List<String> args, String prefix) {
        String needle = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        if (args.isEmpty()) {
            return filter(ROOT, needle);
        }
        String root = args.getFirst().toLowerCase(Locale.ROOT);
        if (args.size() == 1) {
            if ("listeners".equals(root)) {
                return filter(SupportedModEventTypes.simpleNames(), needle);
            }
            if ("trace".equals(root)) {
                return filter(TRACE, needle);
            }
            if ("mod".equals(root)) {
                return filter(modSuggestions(coordinator), needle);
            }
            if ("exceptions".equals(root)) {
                return filter(List.of("1"), needle);
            }
            return List.of();
        }
        if ("mod".equals(root)) {
            return completeMod(coordinator, args, needle);
        }
        if ("listeners".equals(root) && args.size() == 2) {
            return filter(SupportedModEventTypes.simpleNames(), needle);
        }
        if (!"trace".equals(root)) {
            return List.of();
        }
        if (args.size() == 2) {
            return switch (args.get(1).toLowerCase(Locale.ROOT)) {
                case "start" -> ModClientStartCompleter.completeEvent(needle);
                case "stop", "pause", "resume", "restart", "view", "export" ->
                    filter(sessionIds(coordinator, args.get(1)), needle);
                default -> List.of();
            };
        }
        String sub = args.get(1).toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "start" -> ModClientStartCompleter.completeFlags(args, needle);
            case "view" -> completeView(args, needle);
            case "export" -> ModClientExportCompleter.completeFlags(args, needle);
            case "stop", "pause", "resume", "restart" ->
                filter(sessionIds(coordinator, sub), needle);
            default -> List.of();
        };
    }

    private static List<String> completeView(List<String> args, String needle) {
        String previous = args.get(args.size() - 1);
        if ("--dispatch".equalsIgnoreCase(previous) || "--run".equalsIgnoreCase(previous)) {
            return filter(List.of("1", "2", "3", "4", "5"), needle);
        }
        return filter(List.of("--dispatch", "--run"), needle);
    }

    private static List<String> completeMod(ModTraceCoordinator coordinator, List<String> args, String needle) {
        if (args.size() == 2 && "compare".equalsIgnoreCase(args.get(1))) {
            return filter(modIds(coordinator), needle);
        }
        if (args.size() >= 3 && "compare".equalsIgnoreCase(args.get(1))) {
            return filter(modIds(coordinator), needle);
        }
        return List.of();
    }

    private static List<String> modSuggestions(ModTraceCoordinator coordinator) {
        List<String> values = new ArrayList<>();
        values.add("compare");
        values.addAll(modIds(coordinator));
        return values;
    }

    private static List<String> modIds(ModTraceCoordinator coordinator) {
        return coordinator.environmentPort().loadedModVersions().keySet().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private static List<String> sessionIds(ModTraceCoordinator coordinator, String subcommand) {
        boolean restart = "restart".equalsIgnoreCase(subcommand);
        return coordinator.listSessions().stream()
                .filter(session -> !restart || session.state().isTerminal())
                .map(TraceSessionSummary::sessionId)
                .toList();
    }

    public static List<String> completeStartFlagSuggestions(String event, String remaining) {
        return ModClientStartCompleter.completeStartFlagSuggestions(event, remaining);
    }

    public static List<String> completeExportFlagSuggestions(String sessionId, String remaining) {
        return ModClientExportCompleter.completeFlagSuggestions(sessionId, remaining);
    }

    public static List<String> exportArgs(String sessionId, String flags) {
        List<String> args = new ArrayList<>();
        args.add("trace");
        args.add("export");
        args.add(sessionId);
        if (flags != null && !flags.isBlank()) {
            for (String part : flags.trim().split("\\s+")) {
                if (!part.isEmpty()) {
                    args.add(part);
                }
            }
        }
        return args;
    }

    public static List<String> matchingEventNames(String prefix) {
        return filter(SupportedModEventTypes.simpleNames(), prefix == null ? "" : prefix.toLowerCase(Locale.ROOT));
    }

    private static List<String> filter(List<String> values, String prefix) {
        if (prefix.isEmpty()) {
            return values;
        }
        List<String> startsWith = new ArrayList<>();
        List<String> contains = new ArrayList<>();
        for (String value : values) {
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.startsWith(prefix)) {
                startsWith.add(value);
            } else if (lower.contains(prefix)) {
                contains.add(value);
            }
        }
        startsWith.addAll(contains);
        return startsWith;
    }
}
