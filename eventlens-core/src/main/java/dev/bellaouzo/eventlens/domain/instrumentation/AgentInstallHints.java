package dev.bellaouzo.eventlens.domain.instrumentation;

import java.util.ArrayList;
import java.util.List;

public final class AgentInstallHints {

    public static final String README_URL = "https://github.com/bellaouzo/EventLensMC#java-agents-optional";

    private static final String SETUP_GUIDE_PREFIX = "Setup guide: ";

    private AgentInstallHints() {}

    public static String paperJvmArgument(String version) {
        return "-javaagent:eventlens-agent-" + version + ".jar";
    }

    public static String clientJvmArgument(String version) {
        return "-javaagent:eventlens-client-agent-" + version + ".jar";
    }

    public static String observabilityJarName(String version) {
        return "eventlens-observability-" + version + ".jar";
    }

    public static List<String> paperSetupLines(String version) {
        List<String> lines = new ArrayList<>();
        lines.add("Java agent not attached — per-listener timing unavailable.");
        lines.add("Add to the Paper server JVM args:");
        lines.add(paperJvmArgument(version));
        lines.add("Download eventlens-agent-" + version + ".jar from GitHub releases.");
        lines.add("Restart the server (stop, then start — not /reload).");
        lines.add("/eventlens status should show agent: attached and mode: precise.");
        lines.add(SETUP_GUIDE_PREFIX + README_URL);
        return List.copyOf(lines);
    }

    public static List<String> clientSetupLines(String version) {
        List<String> lines = new ArrayList<>();
        lines.add("Client agent not attached — per-mod handler timing unavailable.");
        lines.add("Do NOT put the client agent jar in mods/ — it is not a mod file.");
        lines.add("Add to your Minecraft launcher JVM args (not mods/):");
        lines.add(clientJvmArgument(version));
        lines.add(
                "Optional: place " + observabilityJarName(version) + " next to the agent jar (fat agent also works).");
        lines.add("Restart the client. /eventlens status should show precise.");
        lines.add(SETUP_GUIDE_PREFIX + README_URL);
        return List.copyOf(lines);
    }
}
