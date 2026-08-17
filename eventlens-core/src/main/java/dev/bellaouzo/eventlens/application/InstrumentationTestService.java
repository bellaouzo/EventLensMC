package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.application.port.InstrumentationTestPort;
import java.util.Optional;

public final class InstrumentationTestService {

    private final InstrumentationPort instrumentationPort;
    private final InstrumentationTestPort instrumentationTestPort;

    public InstrumentationTestService(
            InstrumentationPort instrumentationPort, InstrumentationTestPort instrumentationTestPort) {
        this.instrumentationPort = instrumentationPort;
        this.instrumentationTestPort = instrumentationTestPort;
    }

    public TestResult run() {
        Optional<String> agentArg = instrumentationTestPort.resolveAgentArgument();
        return new TestResult(
                instrumentationPort.isAgentPresent(),
                instrumentationPort.protocolVersion(),
                instrumentationPort.isProtocolCompatible(),
                instrumentationPort.listenerSnapshotsEnabled(),
                instrumentationTestPort.canResolveAgentJar(),
                agentArg);
    }

    public record TestResult(
            boolean agentPresent,
            int protocolVersion,
            boolean protocolCompatible,
            boolean snapshotsEnabled,
            boolean resolvableAgentJar,
            Optional<String> agentArgument) {}
}
