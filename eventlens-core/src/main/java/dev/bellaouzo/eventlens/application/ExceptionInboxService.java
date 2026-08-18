package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.exceptions.ExceptionInbox;
import dev.bellaouzo.eventlens.domain.exceptions.ExceptionInboxEntry;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.trace.DispatchCaptureListener;
import java.util.List;

public final class ExceptionInboxService implements DispatchCaptureListener {

    public static final int PAGE_SIZE = 8;

    private final ExceptionInbox inbox = new ExceptionInbox();

    @Override
    public void onDispatchCaptured(String sessionId, TraceDispatchRecord dispatch) {
        boolean agentAbsent = dispatch.partialReasons().contains(TracePartialReason.AGENT_ABSENT);
        if (agentAbsent && dispatch.listenerTimings().isEmpty() && inbox.size() == 0) {
            inbox.add(new ExceptionInboxEntry(
                    dispatch.startedAtMillis(),
                    sessionId,
                    dispatch.eventClassName(),
                    "EventLens",
                    "status",
                    "AGENT_ABSENT",
                    java.util.Optional.empty()));
            return;
        }
        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            if (!timing.threwException()) {
                continue;
            }
            inbox.add(new ExceptionInboxEntry(
                    dispatch.startedAtMillis(),
                    sessionId,
                    dispatch.eventClassName(),
                    timing.pluginName(),
                    timing.methodName(),
                    timing.exceptionType().orElse("Exception"),
                    timing.stackTrace()));
        }
    }

    public List<ExceptionInboxEntry> page(int page) {
        return inbox.page(page, PAGE_SIZE);
    }

    public int size() {
        return inbox.size();
    }

    public int totalPages() {
        int size = inbox.size();
        return size == 0 ? 1 : (int) Math.ceil(size / (double) PAGE_SIZE);
    }
}
