package dev.bellaouzo.eventlens.application.port;

import dev.bellaouzo.eventlens.domain.live.LiveFeedDisplayMode;
import dev.bellaouzo.eventlens.domain.live.LiveFeedLine;
import java.util.List;
import java.util.UUID;

public interface LiveFeedDeliveryPort {

    void deliverChat(UUID viewerId, List<LiveFeedLine> lines);

    void deliverStatus(UUID viewerId, String statusText, LiveFeedDisplayMode displayMode);

    void clearStatus(UUID viewerId);

    void deliverAlert(UUID viewerId, LiveFeedLine line, LiveFeedDisplayMode displayMode);
}
