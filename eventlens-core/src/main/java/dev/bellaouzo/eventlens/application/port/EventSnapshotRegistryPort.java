package dev.bellaouzo.eventlens.application.port;

import java.util.List;

public interface EventSnapshotRegistryPort {

    boolean supportsTrace(String eventClassName);

    List<String> supportedTraceEventSimpleNames();
}
