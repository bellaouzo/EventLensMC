package dev.bellaouzo.eventlens.domain.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DurationStatsCalculatorTest {

    @Test
    void computesPercentilesForFixedSamples() {
        DurationStats stats = DurationStatsCalculator.compute(List.of(100L, 200L, 300L, 400L, 500L));

        assertEquals(5, stats.count());
        assertEquals(100L, stats.minNanos());
        assertEquals(500L, stats.maxNanos());
        assertEquals(300L, stats.averageNanos());
        assertEquals(300L, stats.p50Nanos());
        assertEquals(480L, stats.p95Nanos());
        assertEquals(496L, stats.p99Nanos());
    }
}
