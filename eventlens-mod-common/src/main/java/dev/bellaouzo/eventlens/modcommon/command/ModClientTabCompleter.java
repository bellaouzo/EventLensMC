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
    private static final List<String> START_FLAGS =
            List.of("--confirm-hot", "--max-events", "--mod", "--player");

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
            return List.of();
        }
        if ("listeners".equals(root) && args.size() == 2) {
            return filter(SupportedModEventTypes.simpleNames(), needle);
        }
        if (!"trace".equals(root)) {
            return List.of();
        }
        if (args.size() == 2) {
            return switch (args.get(1).toLowerCase(Locale.ROOT)) {
                case "start" -> filter(SupportedModEventTypes.simpleNames(), needle);
                case "stop", "pause", "resume", "restart", "view", "export" ->
                    filter(sessionIds(coordinator, args.get(1)), needle);
                default -> List.of();
            };
        }
        String sub = args.get(1).toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "start" -> completeStart(args, needle);
            case "view" -> completeView(args, needle);
            case "stop", "pause", "resume", "restart", "export" ->
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

    private static List<String> completeStart(List<String> args, String needle) {
        String previous = args.get(args.size() - 1);
        if ("--max-events".equalsIgnoreCase(previous)) {
            return filter(List.of("32", "64", "128", "256"), needle);
        }
        List<String> remaining = new ArrayList<>(START_FLAGS);
        remaining.removeIf(flag -> containsIgnoreCase(args, flag));
        return filter(remaining, needle);
    }

    private static List<String> sessionIds(ModTraceCoordinator coordinator, String subcommand) {
        boolean restart = "restart".equalsIgnoreCase(subcommand);
        return coordinator.listSessions().stream()
                .filter(session -> !restart || session.state().isTerminal())
                .map(TraceSessionSummary::sessionId)
                .toList();
    }

    private static boolean containsIgnoreCase(List<String> args, String value) {
        for (String arg : args) {
            if (value.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
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
