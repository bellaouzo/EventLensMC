package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.live.LiveFeedChannel;
import dev.bellaouzo.eventlens.domain.live.LiveFeedLimits;
import dev.bellaouzo.eventlens.domain.live.LiveFeedLine;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class LiveFeedBurstDetector {

    private final Map<String, PluginWindow> pluginWindows = new HashMap<>();

    Optional<LiveFeedLine> observe(String pluginName, long nowMillis, int burstThreshold, long burstWindowMillis) {
        PluginWindow window =
                pluginWindows.computeIfAbsent(pluginName.toLowerCase(Locale.ROOT), ignored -> new PluginWindow());
        return window.observe(pluginName, nowMillis, burstThreshold, burstWindowMillis);
    }

    private static final class PluginWindow {
        private int count;
        private long windowStartMillis;
        private long lastAlertMillis;

        Optional<LiveFeedLine> observe(String pluginName, long nowMillis, int burstThreshold, long burstWindowMillis) {
            if (windowStartMillis == 0L || nowMillis - windowStartMillis > burstWindowMillis) {
                windowStartMillis = nowMillis;
                count = 0;
            }
            count++;
            if (count < burstThreshold) {
                return Optional.empty();
            }
            if (nowMillis - lastAlertMillis < LiveFeedLimits.BURST_ALERT_COOLDOWN_MILLIS) {
                return Optional.empty();
            }
            lastAlertMillis = nowMillis;
            count = 0;
            windowStartMillis = nowMillis;
            return Optional.of(new LiveFeedLine(
                    LiveFeedChannel.ALERT,
                    "Burst: " + pluginName + " exceeded " + burstThreshold + " dispatches in "
                            + (burstWindowMillis / 1000) + "s",
                    true));
        }
    }
}
