package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;

@FunctionalInterface
public interface DispatchCaptureListener {

    void onDispatchCaptured(String sessionId, TraceDispatchRecord dispatchRecord);
}
