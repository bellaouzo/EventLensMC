package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.modcommon.ModTracePresets;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ModClientStartCompleter {

    static final List<String> START_FLAGS =
            List.of("--confirm-hot", "--max-events", "--mod", "--player", "--preset");

    private ModClientStartCompleter() {}

    static List<String> completeEvent(String needle) {
        if (needle.startsWith("--")) {
            return filter(START_FLAGS, needle);
        }
        int comma = needle.lastIndexOf(',');
        if (comma >= 0) {
            String head = needle.substring(0, comma + 1);
            String fragment = needle.substring(comma + 1).toLowerCase(Locale.ROOT);
            List<String> replacements = new ArrayList<>();
            for (String match : filter(SupportedModEventTypes.simpleNames(), fragment)) {
                replacements.add(head + match);
            }
            return replacements;
        }
        List<String> values = new ArrayList<>(SupportedModEventTypes.simpleNames());
        values.addAll(ModTracePresets.names());
        return filter(values, needle);
    }

    static List<String> completeFlags(List<String> args, String needle) {
        String previous = args.get(args.size() - 1);
        if ("--max-events".equalsIgnoreCase(previous)) {
            return filter(List.of("32", "64", "128", "256"), needle);
        }
        if ("--preset".equalsIgnoreCase(previous)) {
            return filter(ModTracePresets.names(), needle);
        }
        List<String> remaining = new ArrayList<>(START_FLAGS);
        remaining.removeIf(flag -> containsIgnoreCase(args, flag));
        return filter(remaining, needle);
    }

    static List<String> completeStartFlagSuggestions(String event, String remaining) {
        String raw = remaining == null ? "" : remaining;
        List<String> tokens = new ArrayList<>();
        if (!raw.isBlank()) {
            for (String part : raw.trim().split("\\s+")) {
                if (!part.isEmpty()) {
                    tokens.add(part);
                }
            }
        }
        boolean trailingSpace = raw.endsWith(" ") || raw.isEmpty();
        String fragment = "";
        List<String> completed = new ArrayList<>(tokens);
        if (!trailingSpace && !completed.isEmpty()) {
            fragment = completed.removeLast();
        }
        if (isExactStartFlag(fragment)) {
            completed.add(fragment);
            fragment = "";
        }
        List<String> args = new ArrayList<>();
        args.add("trace");
        args.add("start");
        args.add(event);
        args.addAll(completed);
        List<String> options = completeFlags(args, fragment.toLowerCase(Locale.ROOT));
        String head = String.join(" ", completed);
        List<String> replacements = new ArrayList<>();
        for (String option : options) {
            replacements.add(head.isEmpty() ? option : head + " " + option);
        }
        return replacements;
    }

    private static boolean isExactStartFlag(String token) {
        if (token.isEmpty()) {
            return false;
        }
        for (String flag : START_FLAGS) {
            if (flag.equalsIgnoreCase(token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsIgnoreCase(List<String> args, String value) {
        for (String arg : args) {
            if (value.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
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
