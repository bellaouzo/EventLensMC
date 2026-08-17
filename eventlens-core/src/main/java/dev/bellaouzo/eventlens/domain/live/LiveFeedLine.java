package dev.bellaouzo.eventlens.domain.live;

import org.jspecify.annotations.NonNull;

public record LiveFeedLine(@NonNull LiveFeedChannel channel, @NonNull String text, boolean urgent) {}
