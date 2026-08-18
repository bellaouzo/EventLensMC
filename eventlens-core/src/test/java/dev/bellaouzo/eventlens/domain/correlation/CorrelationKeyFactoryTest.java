package dev.bellaouzo.eventlens.domain.correlation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class CorrelationKeyFactoryTest {

    @Test
    void sameInputsProduceSameKey() {
        Optional<String> first = CorrelationKeyFactory.create(
                Optional.of(CorrelationActionKind.USE_BLOCK),
                Optional.of("uuid-1"),
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.of(10),
                Optional.of(20),
                1_000L);
        Optional<String> second = CorrelationKeyFactory.create(
                Optional.of(CorrelationActionKind.USE_BLOCK),
                Optional.of("uuid-1"),
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.of(10),
                Optional.of(20),
                1_049L);
        assertEquals(first, second);
    }

    @Test
    void bucketEdgeChangesKey() {
        Optional<String> first = CorrelationKeyFactory.create(
                Optional.of(CorrelationActionKind.CHAT),
                Optional.empty(),
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.empty(),
                Optional.empty(),
                100L);
        Optional<String> second = CorrelationKeyFactory.create(
                Optional.of(CorrelationActionKind.CHAT),
                Optional.empty(),
                Optional.of("Steve"),
                Optional.of("world"),
                Optional.empty(),
                Optional.empty(),
                200L);
        assertNotEquals(first, second);
    }

    @Test
    void shareSafeHidesRawIdentity() {
        String raw = "USE_BLOCK|uuid-1|world|10,20|10";
        String safe = CorrelationKeyFactory.shareSafe(raw);
        assertEquals(16, safe.length());
        assertTrue(raw.contains("uuid-1"));
        assertTrue(!safe.contains("uuid"));
    }
}
