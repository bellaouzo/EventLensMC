package dev.bellaouzo.eventlens.setup;

public enum SetupTarget {
    PAPER("Paper server", "EventLens-{version}.jar", "eventlens-agent-{version}.jar", true),
    NEOFORGE("NeoForge client", "eventlens-neoforge-{version}.jar", clientAgentJar(), false),
    FORGE("Forge client", "eventlens-forge-{version}.jar", clientAgentJar(), false),
    FABRIC("Fabric client", "eventlens-fabric-{version}.jar", clientAgentJar(), false);

    private final String label;
    private final String payloadPattern;
    private final String agentPattern;
    private final boolean paper;

    SetupTarget(String label, String payloadPattern, String agentPattern, boolean paper) {
        this.label = label;
        this.payloadPattern = payloadPattern;
        this.agentPattern = agentPattern;
        this.paper = paper;
    }

    String label() {
        return label;
    }

    public boolean paper() {
        return paper;
    }

    String payloadFileName(String version) {
        return payloadPattern.replace("{version}", version);
    }

    String agentFileName(String version) {
        return agentPattern.replace("{version}", version);
    }

    private static String clientAgentJar() {
        return "eventlens-client-agent-{version}.jar";
    }
}
