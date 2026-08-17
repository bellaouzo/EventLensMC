package dev.bellaouzo.eventlens.domain.observability;

import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public final class SamplingPolicy {

    private static final Set<String> HOT_EVENT_CLASS_NAMES = Set.of("org.bukkit.event.player.PlayerMoveEvent");
    private static final int HOT_EVENT_SAMPLE_RATE = 20;

    private final AtomicLong hotEventCounter = new AtomicLong();

    public boolean accepts(String eventClassName, TraceFilter filter) {
        if (!HOT_EVENT_CLASS_NAMES.contains(eventClassName)) {
            return true;
        }
        if (!hasNarrowingFilter(filter)) {
            return false;
        }
        return hotEventCounter.incrementAndGet() % HOT_EVENT_SAMPLE_RATE == 1;
    }

    public boolean isHotEvent(String eventClassName) {
        return HOT_EVENT_CLASS_NAMES.contains(eventClassName);
    }

    public int hotEventSampleRate() {
        return HOT_EVENT_SAMPLE_RATE;
    }

    public static boolean requiresNarrowingFilter(String eventClassName) {
        return HOT_EVENT_CLASS_NAMES.contains(eventClassName);
    }

    private static boolean hasNarrowingFilter(TraceFilter filter) {
        return filter.pluginName().isPresent()
                || filter.playerName().isPresent()
                || filter.worldName().isPresent()
                || filter.region().isPresent();
    }

    public static String hotEventDisplayName() {
        return SupportedEventTypes.displaySimpleName("org.bukkit.event.player.PlayerMoveEvent");
    }
}
