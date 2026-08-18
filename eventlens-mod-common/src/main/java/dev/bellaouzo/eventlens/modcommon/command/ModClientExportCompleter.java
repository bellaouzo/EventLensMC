package dev.bellaouzo.eventlens.modcommon.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ModClientExportCompleter {

    static final List<String> EXPORT_FLAGS = List.of("--format", "--shareable", "--full", "--redacted");
    static final List<String> FORMAT_VALUES = List.of("json", "ndjson", "text", "html");

    private ModClientExportCompleter() {}

    static List<String> completeFlags(List<String> args, String needle) {
        String previous = args.get(args.size() - 1);
        if ("--format".equalsIgnoreCase(previous)) {
            return filter(FORMAT_VALUES, needle);
        }
        List<String> remaining = new ArrayList<>(EXPORT_FLAGS);
        remaining.removeIf(flag -> containsIgnoreCase(args, flag));
        return filter(remaining, needle);
    }

    static List<String> completeFlagSuggestions(String sessionId, String remaining) {
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
        if (isExactExportFlag(fragment)) {
            completed.add(fragment);
            fragment = "";
        }
        List<String> args = new ArrayList<>();
        args.add("trace");
        args.add("export");
        args.add(sessionId);
        args.addAll(completed);
        List<String> options = completeFlags(args, fragment.toLowerCase(Locale.ROOT));
        String head = String.join(" ", completed);
        List<String> replacements = new ArrayList<>();
        for (String option : options) {
            replacements.add(head.isEmpty() ? option : head + " " + option);
        }
        return replacements;
    }

    private static boolean isExactExportFlag(String token) {
        if (token.isEmpty()) {
            return false;
        }
        for (String flag : EXPORT_FLAGS) {
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
