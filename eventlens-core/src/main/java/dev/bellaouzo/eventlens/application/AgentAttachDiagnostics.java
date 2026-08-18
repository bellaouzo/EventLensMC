package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.instrumentation.AgentInstallHints;
import dev.bellaouzo.eventlens.domain.instrumentation.JavaAgentArgumentParser;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class AgentAttachDiagnostics {

    private static final String LEVEL_ERROR = "error";
    private static final String LEVEL_WARN = "warn";
    private static final String LEVEL_INFO = "info";

    public enum Role {
        CLIENT,
        PAPER
    }

    public record Line(String level, String message) {}

    public record Result(List<Line> lines) {}

    private AgentAttachDiagnostics() {}

    public static Result diagnose(Role role, boolean agentAttached) {
        return diagnose(
                role, agentAttached, ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    static Result diagnose(Role role, boolean agentAttached, List<String> jvmArguments) {
        if (agentAttached) {
            return new Result(List.of());
        }

        List<ConfiguredAgent> configured = findConfiguredAgents(role, jvmArguments);
        List<Line> lines = new ArrayList<>();

        if (configured.isEmpty()) {
            lines.add(new Line(
                    LEVEL_INFO, "Instant crash when adding a JVM line usually means Java cannot find that jar path."));
            lines.add(new Line(
                    LEVEL_INFO,
                    "Remove the line to launch again, then paste a full path (C:/.../eventlens-client-agent-"
                            + "<version>.jar)."));
            return new Result(List.copyOf(lines));
        }

        for (ConfiguredAgent configuredAgent : configured) {
            lines.addAll(linesForConfiguredAgent(role, configuredAgent));
        }

        if (lines.stream().noneMatch(line -> LEVEL_ERROR.equals(line.level()) || LEVEL_WARN.equals(line.level()))) {
            lines.add(new Line(
                    LEVEL_INFO,
                    "Agent JVM arg is present but precise timing is off. Check the launcher log for [EventLens]."));
        }

        return new Result(List.copyOf(lines));
    }

    private static List<Line> linesForConfiguredAgent(Role role, ConfiguredAgent configuredAgent) {
        List<Line> lines = new ArrayList<>();
        JavaAgentArgumentParser.Parsed parsed = configuredAgent.parsed();
        Path path = Path.of(parsed.pathText());

        if (role == Role.CLIENT && JavaAgentArgumentParser.mentionsPaperAgent(parsed.rawArgument())) {
            lines.add(new Line(
                    LEVEL_ERROR,
                    "JVM arg looks like the Paper server agent (eventlens-agent), not eventlens-client-agent."));
        }

        if (!path.isAbsolute()) {
            lines.add(new Line(
                    LEVEL_WARN,
                    "Relative -javaagent path: " + parsed.pathText()
                            + " — use a full path so the launcher always finds the jar."));
        }

        if (configuredAgent.pathExists()) {
            if (role == Role.CLIENT) {
                lines.add(new Line(
                        LEVEL_WARN,
                        "Agent jar exists at " + configuredAgent.resolvedPath()
                                + " but did not attach. See launcher log for [EventLens] errors."));
            } else {
                lines.add(new Line(
                        LEVEL_WARN,
                        "Agent jar exists at " + configuredAgent.resolvedPath()
                                + " but Paper reports dispatch-only. See server log for [EventLens]."));
            }
            return lines;
        }

        lines.add(new Line(LEVEL_ERROR, "JVM arg points to a missing file: " + parsed.pathText()));
        lines.add(
                new Line(LEVEL_ERROR, "That missing path causes an instant crash before Minecraft finishes starting."));
        if (path.isAbsolute()) {
            lines.add(new Line(LEVEL_INFO, "Fix or remove this JVM line, then restart: " + parsed.rawArgument()));
        } else {
            lines.add(
                    new Line(
                            LEVEL_INFO,
                            "Use a full path such as C:/Users/You/AppData/Roaming/eventlens-agents/eventlens-client-agent-<version>.jar"));
        }
        return lines;
    }

    private static List<ConfiguredAgent> findConfiguredAgents(Role role, List<String> jvmArguments) {
        List<ConfiguredAgent> configured = new ArrayList<>();
        for (String argument : jvmArguments) {
            Optional<JavaAgentArgumentParser.Parsed> parsed = JavaAgentArgumentParser.parse(argument);
            if (parsed.isEmpty()) {
                continue;
            }
            if (!matchesRole(role, parsed.orElseThrow())) {
                continue;
            }
            configured.add(ConfiguredAgent.resolve(parsed.orElseThrow()));
        }
        return configured;
    }

    private static boolean matchesRole(Role role, JavaAgentArgumentParser.Parsed parsed) {
        String raw = parsed.rawArgument().toLowerCase(Locale.ROOT);
        if (!raw.contains("eventlens")) {
            return false;
        }
        return switch (role) {
            case CLIENT -> raw.contains("eventlens-client-agent") || raw.contains("eventlens-agent");
            case PAPER -> raw.contains("eventlens-agent");
        };
    }

    private record ConfiguredAgent(JavaAgentArgumentParser.Parsed parsed, Path resolvedPath, boolean pathExists) {
        static ConfiguredAgent resolve(JavaAgentArgumentParser.Parsed parsed) {
            Path path = Path.of(parsed.pathText());
            if (Files.isRegularFile(path)) {
                return new ConfiguredAgent(parsed, path.toAbsolutePath().normalize(), true);
            }
            Path relativeToCwd =
                    Path.of(System.getProperty("user.dir", ".")).resolve(path).normalize();
            if (Files.isRegularFile(relativeToCwd)) {
                return new ConfiguredAgent(
                        parsed, relativeToCwd.toAbsolutePath().normalize(), true);
            }
            return new ConfiguredAgent(parsed, path.toAbsolutePath().normalize(), false);
        }
    }

    public static String readmeUrl() {
        return AgentInstallHints.README_URL;
    }
}
