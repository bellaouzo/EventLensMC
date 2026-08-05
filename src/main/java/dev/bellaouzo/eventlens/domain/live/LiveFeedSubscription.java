package dev.bellaouzo.eventlens.domain.live;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

public record LiveFeedSubscription(
        @NonNull String viewerName,
        @NonNull UUID viewerId,
        @NonNull String sessionId,
        @NonNull LiveFeedSettings settings,
        int capturedEvents,
        long subscribedAtMillis) {}
