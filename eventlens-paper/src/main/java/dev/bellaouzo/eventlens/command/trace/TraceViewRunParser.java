package dev.bellaouzo.eventlens.command.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class TraceViewRunParser {

    private static final String RUN_FLAG = "--run";
    private static final String RUN_PREFIX = RUN_FLAG + "=";

    private TraceViewRunParser() {}

    static Result extract(String[] args) {
        int index = indexOfRun(args);
        if (index < 0) {
            return Result.ok(args, Optional.empty());
        }
        String token = args[index];
        boolean inline = token.regionMatches(true, 0, RUN_PREFIX, 0, RUN_PREFIX.length());
        if (!inline && index + 1 >= args.length) {
            return Result.error("Missing value for --run.");
        }
        String value = inline ? token.substring(RUN_PREFIX.length()) : args[index + 1];
        Optional<Integer> generation = parseRun(value);
        if (generation.isEmpty()) {
            return Result.error("--run must be a positive integer.");
        }
        return Result.ok(without(args, index, inline ? index : index + 1), generation);
    }

    private static int indexOfRun(String[] args) {
        for (int i = 3; i < args.length; i++) {
            String token = args[i];
            if (token.equalsIgnoreCase(RUN_FLAG) || token.regionMatches(true, 0, RUN_PREFIX, 0, RUN_PREFIX.length())) {
                return i;
            }
        }
        return -1;
    }

    private static String[] without(String[] args, int first, int second) {
        List<String> remaining = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (i != first && i != second) {
                remaining.add(args[i]);
            }
        }
        return remaining.toArray(String[]::new);
    }

    private static Optional<Integer> parseRun(String value) {
        try {
            int run = Integer.parseInt(value);
            return run < 1 ? Optional.empty() : Optional.of(run - 1);
        } catch (NumberFormatException _) {
            return Optional.empty();
        }
    }

    record Result(Optional<String[]> args, Optional<Integer> generation, Optional<String> errorMessage) {
        static Result ok(String[] args, Optional<Integer> generation) {
            return new Result(Optional.of(args), generation, Optional.empty());
        }

        static Result error(String message) {
            return new Result(Optional.empty(), Optional.empty(), Optional.of(message));
        }
    }
}
