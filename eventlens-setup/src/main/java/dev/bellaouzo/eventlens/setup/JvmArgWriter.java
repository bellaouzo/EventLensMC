package dev.bellaouzo.eventlens.setup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JvmArgWriter {

    private static final Pattern START_JAVA = Pattern.compile("(?i)(\\bjava(?:w)?(?:\\.exe)?)(\\s+)");

    private static final String JVM_ARGS_KEY = "JvmArgs=";
    private static final String OVERRIDE_JAVA_ARGS = "OverrideJavaArgs=true";
    private static final String OVERRIDE_JAVA_ARGS_KEY = "OverrideJavaArgs=";

    private JvmArgWriter() {}

    static String javaAgentArgument(Path agentJar) {
        String path = agentJar.toAbsolutePath().normalize().toString().replace('\\', '/');
        if (path.contains(" ")) {
            return "-javaagent:\"" + path + "\"";
        }
        return "-javaagent:" + path;
    }

    static boolean patchUserJvmArgs(Path file, String javaAgentArg) throws IOException {
        List<String> lines = Files.isRegularFile(file)
                ? new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8))
                : new ArrayList<>();
        boolean replaced = replaceAgentLine(lines, javaAgentArg);
        if (!replaced) {
            if (!lines.isEmpty() && !lines.getLast().isBlank()) {
                lines.add("");
            }
            lines.add("# EventLens Java agent");
            lines.add(javaAgentArg);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
        return true;
    }

    static boolean patchStartScript(Path file, String javaAgentArg) throws IOException {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        String original = Files.readString(file, StandardCharsets.UTF_8);
        String updated = insertIntoJavaCommand(original, javaAgentArg);
        if (updated.equals(original)) {
            return false;
        }
        Files.writeString(file, updated, StandardCharsets.UTF_8);
        return true;
    }

    static boolean patchPrismInstance(Path instanceCfg, String javaAgentArg) throws IOException {
        if (!Files.isRegularFile(instanceCfg)) {
            return false;
        }
        List<String> lines = new ArrayList<>(Files.readAllLines(instanceCfg, StandardCharsets.UTF_8));
        boolean sawOverride = false;
        boolean sawJvmArgs = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(OVERRIDE_JAVA_ARGS_KEY)) {
                lines.set(i, OVERRIDE_JAVA_ARGS);
                sawOverride = true;
            } else if (line.startsWith(JVM_ARGS_KEY)) {
                lines.set(i, JVM_ARGS_KEY + mergeJvmArgs(line.substring(JVM_ARGS_KEY.length()), javaAgentArg));
                sawJvmArgs = true;
            }
        }
        if (!sawOverride) {
            lines.add(OVERRIDE_JAVA_ARGS);
        }
        if (!sawJvmArgs) {
            lines.add(JVM_ARGS_KEY + javaAgentArg);
        }
        Files.write(instanceCfg, lines, StandardCharsets.UTF_8);
        return true;
    }

    static String insertIntoJavaCommand(String script, String javaAgentArg) {
        if (containsEventLensAgent(script)) {
            return script.replaceAll("(?i)-javaagent:\"?[^\s\"]*eventlens-(?:client-)?agent[^\s\"]*\"?", javaAgentArg);
        }
        Matcher matcher = START_JAVA.matcher(script);
        if (!matcher.find()) {
            return script;
        }
        return script.substring(0, matcher.end()) + javaAgentArg + " " + script.substring(matcher.end());
    }

    static String mergeJvmArgs(String existing, String javaAgentArg) {
        String trimmed = stripQuotes(existing.trim());
        if (trimmed.isEmpty()) {
            return javaAgentArg;
        }
        if (containsEventLensAgent(trimmed)) {
            return trimmed.replaceAll("(?i)-javaagent:\"?[^\s\"]*eventlens-(?:client-)?agent[^\s\"]*\"?", javaAgentArg);
        }
        return trimmed + " " + javaAgentArg;
    }

    private static boolean replaceAgentLine(List<String> lines, String javaAgentArg) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (containsEventLensAgent(line)) {
                lines.set(i, javaAgentArg);
                return true;
            }
        }
        return false;
    }

    private static boolean containsEventLensAgent(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        return lower.contains("-javaagent:")
                && (lower.contains("eventlens-agent") || lower.contains("eventlens-client-agent"));
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
