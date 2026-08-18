package dev.bellaouzo.eventlens.setup;

import java.nio.file.Path;

public final class SetupPaths {

    private static final String AGENT_FOLDER = "eventlens-agents";

    private SetupPaths() {}

    public static Path defaultAgentDirectory(SetupTarget target, Path destination) {
        if (target.paper()) {
            return DestinationResolver.serverRoot(destination).resolve(AGENT_FOLDER);
        }
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, AGENT_FOLDER);
        }
        return Path.of(System.getProperty("user.home"), AGENT_FOLDER);
    }

    public static String productVersion() {
        String fromManifest = EventLensSetup.class.getPackage().getImplementationVersion();
        if (fromManifest != null && !fromManifest.isBlank()) {
            return fromManifest;
        }
        return "1.12.0";
    }
}
