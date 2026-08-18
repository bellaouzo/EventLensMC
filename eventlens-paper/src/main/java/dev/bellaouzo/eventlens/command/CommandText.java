package dev.bellaouzo.eventlens.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.NonNull;

public final class CommandText {

    private CommandText() {}

    public static @NonNull String simpleName(@NonNull String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0 || lastDot == className.length() - 1) {
            return className;
        }
        return "" + className.substring(lastDot + 1);
    }

    public static List<String> filterPrefix(List<String> values, String prefix) {
        String needle = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return List.copyOf(values);
        }
        List<String> startsWith = new ArrayList<>();
        List<String> contains = new ArrayList<>();
        for (String value : values) {
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.startsWith(needle)) {
                startsWith.add(value);
            } else if (lower.contains(needle)) {
                contains.add(value);
            }
        }
        startsWith.addAll(contains);
        return startsWith;
    }
}
