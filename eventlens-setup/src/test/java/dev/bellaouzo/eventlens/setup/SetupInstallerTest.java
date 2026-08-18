package dev.bellaouzo.eventlens.setup;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SetupInstallerTest {

    private static final String VERSION = "1.12.0";

    @TempDir
    Path temp;

    @Test
    void paperCopiesPluginPatchesJvmAndLeavesAgentInPlugins() throws Exception {
        Path artifacts = temp.resolve("artifacts");
        writeDummy(artifacts, "EventLens-" + VERSION + ".jar");
        writeDummy(artifacts, "eventlens-agent-" + VERSION + ".jar");
        Path server = temp.resolve("server");
        Path plugins = server.resolve("plugins");
        Files.createDirectories(plugins);
        writeDummy(plugins, "EventLens-1.11.0.jar");
        writeDummy(plugins, "eventlens-agent-old.jar");
        Files.writeString(server.resolve("start.bat"), "java -Xmx2G -jar paper.jar\n");

        Path agents = server.resolve("eventlens-agents");
        SetupInstaller installer = new SetupInstaller(new ArtifactLocator(VERSION, artifacts));
        InstallResult result = installer.install(new InstallRequest(SetupTarget.PAPER, plugins, true, agents, VERSION));

        assertTrue(result.success(), String.join("\n", result.lines()));
        assertTrue(Files.isRegularFile(plugins.resolve("EventLens-" + VERSION + ".jar")));
        assertFalse(Files.exists(plugins.resolve("EventLens-1.11.0.jar")));
        assertTrue(Files.exists(plugins.resolve("eventlens-agent-old.jar")));
        assertTrue(Files.isRegularFile(agents.resolve("eventlens-agent-" + VERSION + ".jar")));
        String userArgs = Files.readString(server.resolve("user_jvm_args.txt"));
        assertTrue(userArgs.contains("-javaagent:"));
        assertTrue(userArgs.contains("eventlens-agent-" + VERSION + ".jar"));
        assertTrue(Files.readString(server.resolve("start.bat")).contains("-javaagent:"));
        assertTrue(result.jvmPatched());
        assertTrue(result.jvmArgument().contains("eventlens-agent-" + VERSION + ".jar"));
        assertFalse(result.needsManualJvmPaste());
    }

    @Test
    void paperCanSkipAgent() throws Exception {
        Path artifacts = temp.resolve("artifacts");
        writeDummy(artifacts, "EventLens-" + VERSION + ".jar");
        SetupInstaller installer = new SetupInstaller(new ArtifactLocator(VERSION, artifacts));
        InstallResult result =
                installer.install(new InstallRequest(SetupTarget.PAPER, temp.resolve("server"), false, null, VERSION));
        assertTrue(result.success(), String.join("\n", result.lines()));
        assertTrue(String.join("\n", result.lines()).contains("Skipped Java agent"));
        assertFalse(result.hasJvmArgument());
        assertFalse(Files.exists(temp.resolve("server").resolve("user_jvm_args.txt")));
    }

    @Test
    void neoForgeCopiesModAndPatchesPrism() throws Exception {
        Path artifacts = temp.resolve("artifacts");
        writeDummy(artifacts, "eventlens-neoforge-" + VERSION + ".jar");
        writeDummy(artifacts, "eventlens-client-agent-" + VERSION + ".jar");
        Path instance = temp.resolve("instance");
        Path mods = instance.resolve(".minecraft").resolve("mods");
        Files.createDirectories(mods);
        writeDummy(mods, "eventlens-neoforge-1.11.0.jar");
        Files.writeString(instance.resolve("instance.cfg"), "name=Test\n");

        Path agents = temp.resolve("agents");
        SetupInstaller installer = new SetupInstaller(new ArtifactLocator(VERSION, artifacts));
        InstallResult result =
                installer.install(new InstallRequest(SetupTarget.NEOFORGE, instance, true, agents, VERSION));

        assertTrue(result.success(), String.join("\n", result.lines()));
        assertTrue(Files.isRegularFile(mods.resolve("eventlens-neoforge-" + VERSION + ".jar")));
        assertFalse(Files.exists(mods.resolve("eventlens-neoforge-1.11.0.jar")));
        String cfg = Files.readString(instance.resolve("instance.cfg"));
        assertTrue(cfg.contains("OverrideJavaArgs=true"));
        assertTrue(cfg.contains("eventlens-client-agent-" + VERSION + ".jar"));
        assertTrue(result.jvmPatched());
        assertFalse(result.needsManualJvmPaste());
    }

    @Test
    void clientWithoutPrismRequiresManualJvmPaste() throws Exception {
        Path artifacts = temp.resolve("artifacts");
        writeDummy(artifacts, "eventlens-neoforge-" + VERSION + ".jar");
        writeDummy(artifacts, "eventlens-client-agent-" + VERSION + ".jar");
        Path mods = temp.resolve("mods");
        Files.createDirectories(mods);
        SetupInstaller installer = new SetupInstaller(new ArtifactLocator(VERSION, artifacts));
        InstallResult result = installer.install(
                new InstallRequest(SetupTarget.NEOFORGE, mods, true, temp.resolve("agents"), VERSION));
        assertTrue(result.success(), String.join("\n", result.lines()));
        assertTrue(result.needsManualJvmPaste());
        assertTrue(result.jvmArgument().startsWith("-javaagent:"));
    }

    @Test
    void missingArtifactFailsClearly() {
        SetupInstaller installer = new SetupInstaller(new ArtifactLocator(VERSION, temp.resolve("empty")));
        InstallResult result =
                installer.install(new InstallRequest(SetupTarget.PAPER, temp.resolve("server"), false, null, VERSION));
        assertFalse(result.success());
        assertTrue(result.lines().getFirst().contains("EventLens-" + VERSION + ".jar"));
    }

    private static void writeDummy(Path directory, String name) throws Exception {
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(name), name);
    }
}
