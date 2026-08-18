package dev.bellaouzo.eventlens.setup;

import java.nio.file.Path;

public record InstallRequest(
        SetupTarget target, Path destination, boolean installAgent, Path agentDirectory, String version) {

    public InstallRequest {
        if (target == null || destination == null || version == null || version.isBlank()) {
            throw new IllegalArgumentException("target, destination, and version are required");
        }
        if (installAgent && agentDirectory == null) {
            throw new IllegalArgumentException("agent directory is required when installing an agent");
        }
    }
}
