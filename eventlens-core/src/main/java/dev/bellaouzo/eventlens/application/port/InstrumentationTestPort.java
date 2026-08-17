package dev.bellaouzo.eventlens.application.port;

import java.util.Optional;

public interface InstrumentationTestPort {

    Optional<String> resolveAgentArgument();

    boolean canResolveAgentJar();
}
