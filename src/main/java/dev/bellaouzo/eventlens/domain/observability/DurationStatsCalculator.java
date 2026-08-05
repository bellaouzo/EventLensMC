package dev.bellaouzo.eventlens.domain.observability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DurationStatsCalculator {

    private DurationStatsCalculator() {}

    public static DurationStats compute(List<Long> samples) {
        if (samples == null || samples.isEmpty()) {
            return DurationStats.empty();
        }

        List<Long> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);

        long min = sorted.getFirst();
        long max = sorted.getLast();
        long sum = 0L;
        for (long sample : sorted) {
            sum += sample;
        }
        long average = sum / sorted.size();

        return new DurationStats(
                sorted.size(),
                min,
                max,
                average,
                percentile(sorted, 50),
                percentile(sorted, 95),
                percentile(sorted, 99));
    }

    private static long percentile(List<Long> sorted, int percentile) {
        if (sorted.isEmpty()) {
            return 0L;
        }
        if (sorted.size() == 1) {
            return sorted.getFirst();
        }
        double rank = (percentile / 100.0) * (sorted.size() - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);
        if (lowerIndex == upperIndex) {
            return sorted.get(lowerIndex);
        }
        double weight = rank - lowerIndex;
        long lower = sorted.get(lowerIndex);
        long upper = sorted.get(upperIndex);
        return Math.round(lower + (upper - lower) * weight);
    }
}
