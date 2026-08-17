package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationCapabilities;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationDiagnosticLine;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationDiagnostics;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class InstrumentationQueryService {

    private final InstrumentationPort instrumentationPort;
    private final String expectedPlatform;

    public InstrumentationQueryService(InstrumentationPort instrumentationPort, String expectedPlatform) {
        this.instrumentationPort = instrumentationPort;
        this.expectedPlatform = expectedPlatform;
    }

    public InstrumentationDiagnostics query(String paperVersionReported, String bukkitVersionReported) {
        boolean agentPresent = instrumentationPort.isAgentPresent();
        int protocolVersion = agentPresent ? instrumentationPort.protocolVersion() : 0;
        boolean protocolCompatible = instrumentationPort.isProtocolCompatible();
        boolean paperCompatible =
                PaperVersionSupport.isCompatible(expectedPlatform, paperVersionReported, bukkitVersionReported);
        boolean snapshotsEnabled = instrumentationPort.listenerSnapshotsEnabled();

        InstrumentationMode mode = resolveMode(agentPresent, protocolCompatible, snapshotsEnabled);
        InstrumentationCapabilities capabilities = resolveCapabilities(mode, snapshotsEnabled);
        List<InstrumentationDiagnosticLine> lines =
                buildLines(mode, agentPresent, protocolCompatible, paperCompatible, snapshotsEnabled, protocolVersion);

        return new InstrumentationDiagnostics(
                mode,
                capabilities,
                agentPresent,
                protocolVersion,
                protocolCompatible,
                paperCompatible,
                paperVersionReported,
                bukkitVersionReported,
                expectedPlatform,
                lines);
    }

    private static InstrumentationMode resolveMode(
            boolean agentPresent, boolean protocolCompatible, boolean snapshotsEnabled) {
        if (!agentPresent) {
            return InstrumentationMode.DISPATCH_ONLY;
        }
        if (!protocolCompatible || !snapshotsEnabled) {
            return InstrumentationMode.DEGRADED;
        }
        return InstrumentationMode.PRECISE;
    }

    private static InstrumentationCapabilities resolveCapabilities(InstrumentationMode mode, boolean snapshotsEnabled) {
        return switch (mode) {
            case PRECISE -> InstrumentationCapabilities.precise();
            case DISPATCH_ONLY -> InstrumentationCapabilities.dispatchOnly();
            case DEGRADED -> InstrumentationCapabilities.degraded(snapshotsEnabled);
        };
    }

    private static List<InstrumentationDiagnosticLine> buildLines(
            InstrumentationMode mode,
            boolean agentPresent,
            boolean protocolCompatible,
            boolean paperCompatible,
            boolean snapshotsEnabled,
            int protocolVersion) {
        List<InstrumentationDiagnosticLine> lines = new ArrayList<>();

        lines.add(new InstrumentationDiagnosticLine(
                "info",
                "MODE",
                "Instrumentation mode: " + mode.name().toLowerCase(Locale.ROOT).replace('_', '-')));

        if (!agentPresent) {
            lines.add(new InstrumentationDiagnosticLine(
                    "warn",
                    "AGENT_ABSENT",
                    "Java agent not loaded. Using priority-band snapshots and dispatch timing only."));
            lines.add(
                    new InstrumentationDiagnosticLine(
                            "info",
                            "FALLBACK",
                            "Attach -javaagent:eventlens-agent.jar for per-listener duration, snapshots, and cancellation timeline."));
        } else {
            lines.add(new InstrumentationDiagnosticLine(
                    "info", "AGENT_ATTACHED", "Java agent attached (protocol " + protocolVersion + ")."));
            if (!protocolCompatible) {
                lines.add(new InstrumentationDiagnosticLine(
                        "error", "INCOMPATIBLE_PROTOCOL", "Agent protocol is incompatible with the plugin build."));
            }
            if (!snapshotsEnabled) {
                lines.add(new InstrumentationDiagnosticLine(
                        "warn",
                        "SNAPSHOT_BRIDGE_MISSING",
                        "Per-listener snapshots unavailable until the plugin registers the snapshot bridge."));
            }
        }

        if (!paperCompatible) {
            lines.add(
                    new InstrumentationDiagnosticLine(
                            "warn",
                            "PAPER_VERSION_MISMATCH",
                            "Server version may differ from tested platform; verify compatibility before relying on agent transforms."));
        }

        lines.add(new InstrumentationDiagnosticLine(
                "info",
                "EXCEPTION_POLICY",
                "Listener exceptions propagate unchanged; EventLens only records exception type and timing."));
        lines.add(new InstrumentationDiagnosticLine(
                "info",
                "FALLBACK_BANDS",
                "Without per-listener snapshots, property changes are attributed to priority bands (LOW..MONITOR)."));

        return List.copyOf(lines);
    }
}
