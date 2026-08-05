package dev.bellaouzo.eventlens.application;

import java.util.Optional;

final class LiveFeedFrequencyBuffer {

    private int count;
    private long windowStartMillis;
    private String lastEventLabel = "";

    synchronized void track(String eventLabel, long nowMillis) {
        if (count == 0) {
            windowStartMillis = nowMillis;
        }
        count++;
        lastEventLabel = eventLabel;
    }

    synchronized Optional<FrequencySummary> flushIfDue(long nowMillis, long aggregateWindowMillis) {
        if (count == 0 || nowMillis - windowStartMillis < aggregateWindowMillis) {
            return Optional.empty();
        }
        return Optional.of(takeSummary(nowMillis));
    }

    synchronized Optional<FrequencySummary> flushRemaining(long nowMillis) {
        if (count == 0) {
            return Optional.empty();
        }
        return Optional.of(takeSummary(nowMillis));
    }

    private FrequencySummary takeSummary(long nowMillis) {
        FrequencySummary summary =
                new FrequencySummary(lastEventLabel, count, Math.max(1L, nowMillis - windowStartMillis));
        count = 0;
        windowStartMillis = nowMillis;
        return summary;
    }

    record FrequencySummary(String eventLabel, int count, long windowMillis) {}
}
