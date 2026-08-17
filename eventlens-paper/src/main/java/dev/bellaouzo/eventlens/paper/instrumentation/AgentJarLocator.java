package dev.bellaouzo.eventlens.paper.instrumentation;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.plugin.Plugin;

public final class AgentJarLocator {

    private static final String JAVA_AGENT_PREFIX = "-javaagent:";
    private static final String BUILD_DIR = "build";
    private static final String LIBS_DIR = "libs";

    private AgentJarLocator() {}

    public static Optional<Path> locate(Plugin plugin) {
        Optional<Path> fromJvm = locateFromJvmArguments();
        if (fromJvm.isPresent()) {
            return fromJvm;
        }
        return locateFromFilesystem(plugin);
    }

    public static Optional<String> resolveAgentArgument(Plugin plugin) {
        return locateFromJvmArguments().or(() -> locate(plugin)).map(path -> JAVA_AGENT_PREFIX + path.toAbsolutePath());
    }

    private static Optional<Path> locateFromJvmArguments() {
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (!argument.startsWith(JAVA_AGENT_PREFIX)) {
                continue;
            }
            String pathText = argument.substring(JAVA_AGENT_PREFIX.length());
            int optionSeparator = pathText.indexOf('=');
            if (optionSeparator >= 0) {
                pathText = pathText.substring(0, optionSeparator);
            }
            Path path = Path.of(pathText);
            if (Files.isRegularFile(path)) {
                return Optional.of(path);
            }
        }
        return Optional.empty();
    }

    private static Optional<Path> locateFromFilesystem(Plugin plugin) {
        Path root = plugin.getDataFolder().toPath().getParent();
        if (root == null) {
            return Optional.empty();
        }
        Path sibling = root.resolve("eventlens-agent").resolve(BUILD_DIR).resolve(LIBS_DIR);
        if (Files.isDirectory(sibling)) {
            Optional<Path> found = firstJarMatching(sibling);
            if (found.isPresent()) {
                return found;
            }
        }
        Path localBuild = root.resolve(BUILD_DIR).resolve(LIBS_DIR);
        if (Files.isDirectory(localBuild)) {
            return firstJarMatching(localBuild);
        }
        Path userDirAgent = Path.of(System.getProperty("user.dir", "."))
                .resolve("eventlens-agent")
                .resolve(BUILD_DIR)
                .resolve(LIBS_DIR);
        if (Files.isDirectory(userDirAgent)) {
            return firstJarMatching(userDirAgent);
        }
        return Optional.empty();
    }

    private static Optional<Path> firstJarMatching(Path directory) {
        try (var paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName()
                            .toString()
                            .toLowerCase(Locale.ROOT)
                            .contains("agent"))
                    .filter(path -> path.getFileName()
                            .toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".jar"))
                    .sorted()
                    .findFirst();
        } catch (java.io.IOException _) {
            return Optional.empty();
        }
    }
}
