package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.application.AgentAttachDiagnostics;
import dev.bellaouzo.eventlens.domain.instrumentation.AgentInstallHints;
import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import java.util.ArrayList;
import java.util.List;

public final class ModStatusHover {

    private ModStatusHover() {}

    public static String tracingLabel(ModTraceResults.Status status) {
        if (status.tracingEnabled()) {
            return "live";
        }
        if (pausedCount(status) > 0) {
            return "paused";
        }
        return "idle";
    }

    public static List<String> tracingLines(ModTraceResults.Status status) {
        List<String> lines = new ArrayList<>();
        String label = tracingLabel(status);
        if ("live".equals(label)) {
            lines.add("Tracing live");
            lines.add(status.activeSessionCount() + " active session(s)");
        } else if ("paused".equals(label)) {
            lines.add("Tracing paused");
            lines.add(pausedCount(status) + " session(s) waiting");
            lines.add("Resume to capture again");
        } else {
            lines.add("Tracing idle");
            lines.add("No open sessions");
            lines.add("Start an event to capture");
        }
        lines.add("EventLens " + status.version());
        return lines;
    }

    public static List<String> instrumentationLines(ModTraceResults.Status status) {
        return instrumentationLines(status, ModRuntimeKind.NEOFORGE);
    }

    public static List<String> instrumentationLines(ModTraceResults.Status status, ModRuntimeKind runtimeKind) {
        List<String> lines = new ArrayList<>();
        if (status.agentPresent()) {
            lines.add("Client agent attached");
            lines.add("Protocol " + status.agentProtocolVersion()
                    + (status.agentProtocolCompatible() ? " (compatible)" : " (incompatible)"));
            lines.add(status.snapshotsEnabled() ? "Per-mod handler timing on" : "Handler timing without snapshots");
        } else {
            lines.add("Client agent not attached — per-mod handler timing unavailable.");
            for (AgentAttachDiagnostics.Line line : AgentAttachDiagnostics.diagnose(
                            AgentAttachDiagnostics.Role.CLIENT, status.agentPresent())
                    .lines()) {
                lines.add(line.message());
            }
            if (runtimeKind == ModRuntimeKind.FABRIC) {
                lines.add("Fabric client agent is not supported yet.");
            } else {
                lines.add("Launcher JVM arg (not mods/): " + AgentInstallHints.clientJvmArgument(status.version()));
            }
        }
        lines.add("EventLens " + status.version());
        return lines;
    }

    public static int pausedCount(ModTraceResults.Status status) {
        int count = 0;
        for (var session : status.sessions()) {
            if (session.state() == TraceSessionState.PAUSED) {
                count++;
            }
        }
        return count;
    }
}
