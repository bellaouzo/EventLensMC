package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.LiveFeedDeliveryPort;
import dev.bellaouzo.eventlens.domain.live.LiveFeedChannel;
import dev.bellaouzo.eventlens.domain.live.LiveFeedDisplayMode;
import dev.bellaouzo.eventlens.domain.live.LiveFeedLine;
import dev.bellaouzo.eventlens.domain.live.LiveFeedSettings;
import dev.bellaouzo.eventlens.domain.live.LiveFeedSubscription;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class LiveFeedDispatchProcessor {

    private final LiveFeedDeliveryPort deliveryPort;
    private final LiveFeedConfig liveFeedConfig;
    private final Map<String, LiveFeedFrequencyBuffer> frequencyBuffers;
    private final Map<String, LiveFeedBurstDetector> burstDetectors;
    private final Map<String, Integer> captureCounts;

    LiveFeedDispatchProcessor(
            LiveFeedDeliveryPort deliveryPort,
            LiveFeedConfig liveFeedConfig,
            Map<String, LiveFeedFrequencyBuffer> frequencyBuffers,
            Map<String, LiveFeedBurstDetector> burstDetectors,
            Map<String, Integer> captureCounts) {
        this.deliveryPort = deliveryPort;
        this.liveFeedConfig = liveFeedConfig;
        this.frequencyBuffers = frequencyBuffers;
        this.burstDetectors = burstDetectors;
        this.captureCounts = captureCounts;
    }

    void processDispatch(
            LiveFeedSubscription subscription,
            TraceDispatchRecord dispatch,
            TraceSessionConfig sessionConfig,
            long nowMillis) {
        captureCounts.merge(subscription.viewerName(), 1, Integer::sum);
        LiveFeedSettings settings = subscription.settings();
        if (settings.paused()) {
            return;
        }

        long slowThreshold =
                settings.slowThresholdNanos() > 0L ? settings.slowThresholdNanos() : sessionConfig.slowThresholdNanos();
        String eventLabel = simpleEventName(dispatch.eventClassName());

        if (settings.acceptsChannel(LiveFeedChannel.FREQUENCY)) {
            LiveFeedFrequencyBuffer buffer = frequencyBuffers.get(subscription.viewerName());
            if (buffer != null) {
                buffer.track(eventLabel, nowMillis);
            }
        }

        List<LiveFeedLine> immediate = new ArrayList<>(
                LiveFeedAnalyzer.analyze(dispatch, slowThreshold, settings.pluginFilter(), false).stream()
                        .filter(line -> settings.acceptsChannel(LiveFeedChannel.FREQUENCY)
                                || line.channel() != LiveFeedChannel.FREQUENCY)
                        .toList());

        if (settings.acceptsChannel(LiveFeedChannel.ALERT)) {
            immediate.addAll(LiveFeedAnalyzer.analyzeAlerts(dispatch, slowThreshold, settings.pluginFilter()));
            detectBursts(subscription, dispatch, nowMillis, settings).ifPresent(immediate::add);
        }

        deliverLines(subscription, filterByChannels(immediate, settings));
    }

    void flushFrequencyBuffers(Iterable<LiveFeedSubscription> subscriptions, long nowMillis) {
        for (LiveFeedSubscription subscription : subscriptions) {
            if (!subscription.settings().paused()
                    && subscription.settings().acceptsChannel(LiveFeedChannel.FREQUENCY)) {
                LiveFeedFrequencyBuffer buffer = frequencyBuffers.get(subscription.viewerName());
                if (buffer != null) {
                    buffer.flushIfDue(nowMillis, subscription.settings().aggregateWindowMillis())
                            .ifPresent(summary -> deliverLines(
                                    subscription,
                                    List.of(new LiveFeedLine(
                                            LiveFeedChannel.FREQUENCY,
                                            summary.eventLabel() + " x" + summary.count() + " in "
                                                    + (summary.windowMillis() / 1000) + "s",
                                            false))));
                }
            }
        }
    }

    void updateStatusBars(Iterable<LiveFeedSubscription> subscriptions) {
        for (LiveFeedSubscription subscription : subscriptions) {
            LiveFeedDisplayMode mode = subscription.settings().displayMode();
            if (mode == LiveFeedDisplayMode.CHAT) {
                continue;
            }
            int captured = captureCounts.getOrDefault(subscription.viewerName(), subscription.capturedEvents());
            String status = "EventLens live " + subscription.sessionId() + " | captured " + captured
                    + (subscription.settings().paused() ? " | paused" : "");
            deliveryPort.deliverStatus(subscription.viewerId(), status, mode);
        }
    }

    private Optional<LiveFeedLine> detectBursts(
            LiveFeedSubscription subscription,
            TraceDispatchRecord dispatch,
            long nowMillis,
            LiveFeedSettings settings) {
        LiveFeedBurstDetector detector = burstDetectors.get(subscription.viewerName());
        if (detector == null) {
            return Optional.empty();
        }
        Map<String, Integer> pluginCounts = new HashMap<>();
        dispatch.listenerTimings().forEach(timing -> pluginCounts.merge(timing.pluginName(), 1, Integer::sum));
        dispatch.listenerChain().forEach(listener -> pluginCounts.merge(listener.pluginName(), 1, Integer::sum));
        if (pluginCounts.isEmpty()) {
            pluginCounts.put("unknown", 1);
        }
        for (Map.Entry<String, Integer> entry : pluginCounts.entrySet()) {
            if (!matchesPluginFilter(settings.pluginFilter(), entry.getKey())) {
                continue;
            }
            Optional<LiveFeedLine> alert = detector.observe(
                    entry.getKey(), nowMillis, settings.burstThreshold(), settings.burstWindowMillis());
            if (alert.isPresent()) {
                return alert;
            }
        }
        return Optional.empty();
    }

    private void deliverLines(LiveFeedSubscription subscription, List<LiveFeedLine> lines) {
        if (lines.isEmpty()) {
            return;
        }
        LiveFeedDisplayMode mode = subscription.settings().displayMode();
        List<LiveFeedLine> chatLines = new ArrayList<>();
        for (LiveFeedLine line : lines) {
            if (line.urgent() && mode != LiveFeedDisplayMode.CHAT) {
                deliveryPort.deliverAlert(subscription.viewerId(), line, mode);
            } else {
                chatLines.add(line);
            }
        }
        if (!chatLines.isEmpty()) {
            int limit = Math.min(chatLines.size(), liveFeedConfig.maxLinesPerTick());
            deliveryPort.deliverChat(subscription.viewerId(), chatLines.subList(0, limit));
        }
    }

    private static List<LiveFeedLine> filterByChannels(List<LiveFeedLine> lines, LiveFeedSettings settings) {
        return lines.stream()
                .filter(line -> settings.acceptsChannel(line.channel()))
                .toList();
    }

    private static boolean matchesPluginFilter(Optional<String> pluginFilter, String pluginName) {
        return pluginFilter.map(pluginName::equalsIgnoreCase).orElse(true);
    }

    private static String simpleEventName(String eventClassName) {
        int lastDot = eventClassName.lastIndexOf('.');
        return lastDot >= 0 ? eventClassName.substring(lastDot + 1) : eventClassName;
    }
}
