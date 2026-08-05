package dev.bellaouzo.eventlens.command.trace;

import java.util.Locale;
import java.util.Optional;

final class TraceExportOptionReaders {

    private TraceExportOptionReaders() {}

    static CopyDispatchOptionResult parseCopyDispatchOption(String[] args, int flagStartIndex) {
        for (int index = flagStartIndex; index < args.length; index++) {
            String token = args[index];
            if (token.equalsIgnoreCase("--dispatch")) {
                if (index + 1 >= args.length) {
                    return CopyDispatchOptionResult.error("Missing value for --dispatch.");
                }
                return parseDispatchValue(args[index + 1]);
            }
            if (token.toLowerCase(Locale.ROOT).startsWith("--dispatch=")) {
                return parseDispatchValue(token.substring("--dispatch=".length()));
            }
        }
        return CopyDispatchOptionResult.ok(Optional.empty());
    }

    static ComparePluginScopeResult parseComparePluginScope(String[] args, int flagStartIndex) {
        for (int index = flagStartIndex; index < args.length; index++) {
            String token = args[index];
            if (token.equalsIgnoreCase("--plugin")) {
                if (index + 1 >= args.length) {
                    return ComparePluginScopeResult.error("Missing value for --plugin.");
                }
                String value = args[index + 1].trim();
                if (value.isEmpty()) {
                    return ComparePluginScopeResult.error("Plugin filter cannot be empty.");
                }
                return ComparePluginScopeResult.ok(Optional.of(value));
            }
            if (token.toLowerCase(Locale.ROOT).startsWith("--plugin=")) {
                String value = token.substring("--plugin=".length()).trim();
                if (value.isEmpty()) {
                    return ComparePluginScopeResult.error("Plugin filter cannot be empty.");
                }
                return ComparePluginScopeResult.ok(Optional.of(value));
            }
        }
        return ComparePluginScopeResult.ok(Optional.empty());
    }

    private static CopyDispatchOptionResult parseDispatchValue(String value) {
        long sequence;
        try {
            sequence = Long.parseLong(value);
        } catch (NumberFormatException _) {
            return CopyDispatchOptionResult.error("Dispatch must be a positive integer.");
        }
        if (sequence < 1L) {
            return CopyDispatchOptionResult.error("Dispatch must be a positive integer.");
        }
        return CopyDispatchOptionResult.ok(Optional.of(sequence));
    }

    record CopyDispatchOptionResult(Optional<Long> dispatchSequence, Optional<String> errorMessage) {
        static CopyDispatchOptionResult ok(Optional<Long> dispatchSequence) {
            return new CopyDispatchOptionResult(dispatchSequence, Optional.empty());
        }

        static CopyDispatchOptionResult error(String errorMessage) {
            return new CopyDispatchOptionResult(Optional.empty(), Optional.of(errorMessage));
        }
    }

    record ComparePluginScopeResult(Optional<String> pluginScope, Optional<String> errorMessage) {
        static ComparePluginScopeResult ok(Optional<String> pluginScope) {
            return new ComparePluginScopeResult(pluginScope, Optional.empty());
        }

        static ComparePluginScopeResult error(String message) {
            return new ComparePluginScopeResult(Optional.empty(), Optional.of(message));
        }
    }
}
