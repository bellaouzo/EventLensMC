package dev.bellaouzo.eventlens.paper.snapshot;

import dev.bellaouzo.eventlens.application.port.EventSnapshotRegistryPort;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import java.util.List;

public final class PaperEventSnapshotRegistry implements EventSnapshotRegistryPort {

    @Override
    public boolean supportsTrace(String eventClassName) {
        return SupportedEventTypes.isSupported(eventClassName);
    }

    @Override
    public List<String> supportedTraceEventSimpleNames() {
        return SupportedEventTypes.simpleNames();
    }
}
