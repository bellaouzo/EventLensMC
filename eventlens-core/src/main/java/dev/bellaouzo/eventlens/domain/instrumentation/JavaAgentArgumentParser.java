package dev.bellaouzo.eventlens.domain.instrumentation;

import java.util.Locale;
import java.util.Optional;

public final class JavaAgentArgumentParser {

    public static final String JAVA_AGENT_PREFIX = "-javaagent:";

    private JavaAgentArgumentParser() {}

    public record Parsed(String rawArgument, String pathText) {}

    public static Optional<Parsed> parse(String jvmArgument) {
        if (jvmArgument == null || !jvmArgument.startsWith(JAVA_AGENT_PREFIX)) {
            return Optional.empty();
        }
        String pathText = jvmArgument.substring(JAVA_AGENT_PREFIX.length()).trim();
        if (pathText.isEmpty()) {
            return Optional.empty();
        }
        int optionSeparator = pathText.indexOf('=');
        if (optionSeparator >= 0) {
            pathText = pathText.substring(0, optionSeparator).trim();
        }
        if (pathText.startsWith("\"") && pathText.endsWith("\"") && pathText.length() > 1) {
            pathText = pathText.substring(1, pathText.length() - 1);
        }
        if (pathText.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Parsed(jvmArgument, pathText));
    }

    public static boolean mentionsClientAgent(String rawArgument) {
        return containsIgnoreCase(rawArgument, "eventlens-client-agent");
    }

    public static boolean mentionsPaperAgent(String rawArgument) {
        return containsIgnoreCase(rawArgument, "eventlens-agent")
                && !containsIgnoreCase(rawArgument, "eventlens-client-agent");
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }
}
