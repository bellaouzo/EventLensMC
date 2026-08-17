package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import java.util.List;

public interface ModListenerTimingSource {

    List<ListenerTimingRecord> consume(long observationKey, long slowThresholdNanos);
}
