package dev.bellaouzo.eventlens.command;

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
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                .toList();
    }
}
