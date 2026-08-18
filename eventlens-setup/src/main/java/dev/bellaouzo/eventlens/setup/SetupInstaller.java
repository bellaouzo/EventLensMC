package dev.bellaouzo.eventlens.setup;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SetupInstaller {

    private final ArtifactLocator artifacts;

    public SetupInstaller(ArtifactLocator artifacts) {
        this.artifacts = artifacts;
    }

    public InstallResult install(InstallRequest request) {
        List<String> lines = new ArrayList<>();
        try {
            Path payloadDir = DestinationResolver.payloadDirectory(request.target(), request.destination());
            Files.createDirectories(payloadDir);
            removeOldPayloads(payloadDir, request.target());
            Path payload = payloadDir.resolve(request.target().payloadFileName(request.version()));
            artifacts.copyTo(request.target().payloadFileName(request.version()), payload);
            lines.add("Copied " + payload.getFileName() + " to " + payloadDir);

            String jvmArgument = null;
            boolean jvmPatched = false;
            if (request.installAgent()) {
                AgentInstall agent = installAgent(request, lines);
                jvmArgument = agent.argument();
                jvmPatched = agent.patched();
            } else {
                lines.add("Skipped Java agent. /eventlens status will say dispatch-only.");
            }
            lines.add("");
            lines.add("Next: fully restart the server or quit the launcher and start again.");
            lines.add("Then run /eventlens status — you want version " + request.version() + ".");
            if (request.installAgent()) {
                lines.add("If you attached the agent, status should say precise.");
            }
            return InstallResult.ok(lines, jvmArgument, jvmPatched);
        } catch (IOException ex) {
            return InstallResult.fail(ex.getMessage() == null ? "Install failed." : ex.getMessage());
        }
    }

    private AgentInstall installAgent(InstallRequest request, List<String> lines) throws IOException {
        Files.createDirectories(request.agentDirectory());
        Path agentJar = request.agentDirectory().resolve(request.target().agentFileName(request.version()));
        artifacts.copyTo(request.target().agentFileName(request.version()), agentJar);
        String arg = JvmArgWriter.javaAgentArgument(agentJar);
        lines.add("Copied " + agentJar.getFileName() + " to " + request.agentDirectory());
        boolean patched = patchJvm(request, arg, lines);
        return new AgentInstall(arg, patched);
    }

    private record AgentInstall(String argument, boolean patched) {}

    private static boolean patchJvm(InstallRequest request, String arg, List<String> lines) throws IOException {
        if (request.target().paper()) {
            return patchPaper(DestinationResolver.serverRoot(request.destination()), arg, lines);
        }
        var prism = DestinationResolver.prismInstanceRoot(request.destination());
        if (prism.isPresent()) {
            Path cfg = prism.get().resolve("instance.cfg");
            JvmArgWriter.patchPrismInstance(cfg, arg);
            lines.add("Updated Prism/MultiMC " + cfg);
            return true;
        }
        return false;
    }

    private static boolean patchPaper(Path serverRoot, String arg, List<String> lines) throws IOException {
        boolean patched = false;
        Path userArgs = serverRoot.resolve("user_jvm_args.txt");
        JvmArgWriter.patchUserJvmArgs(userArgs, arg);
        lines.add("Updated " + userArgs);
        patched = true;
        for (String name : List.of("start.bat", "start.cmd", "start.sh", "start.ps1")) {
            Path script = serverRoot.resolve(name);
            if (JvmArgWriter.patchStartScript(script, arg)) {
                lines.add("Updated " + script);
            }
        }
        return patched;
    }

    private static void removeOldPayloads(Path directory, SetupTarget target) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.jar")) {
            for (Path jar : stream) {
                String name = jar.getFileName().toString().toLowerCase(Locale.ROOT);
                if (shouldReplace(target, name)) {
                    Files.deleteIfExists(jar);
                }
            }
        }
    }

    private static boolean shouldReplace(SetupTarget target, String fileName) {
        return switch (target) {
            case PAPER ->
                fileName.startsWith("eventlens-")
                        && !fileName.contains("agent")
                        && !fileName.contains("neoforge")
                        && !fileName.contains("-forge-")
                        && !fileName.contains("fabric")
                        && !fileName.contains("setup")
                        && !fileName.contains("observability");
            case NEOFORGE -> fileName.startsWith("eventlens-neoforge-");
            case FORGE -> fileName.startsWith("eventlens-forge-");
            case FABRIC -> fileName.startsWith("eventlens-fabric-");
        };
    }
}
