package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.LiveFeedDeliveryPort;
import dev.bellaouzo.eventlens.domain.live.LiveFeedCommandResult;
import dev.bellaouzo.eventlens.domain.live.LiveFeedLimits;
import dev.bellaouzo.eventlens.domain.live.LiveFeedSettings;
import dev.bellaouzo.eventlens.domain.live.LiveFeedSubscription;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.trace.DispatchCaptureListener;
import dev.bellaouzo.eventlens.trace.SessionLifecycleListener;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TraceLiveFeedService implements DispatchCaptureListener, SessionLifecycleListener {

    private final TraceSessionManager traceSessionManager;
    private final LiveFeedDeliveryPort deliveryPort;
    private final Map<String, LiveFeedSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, LiveFeedFrequencyBuffer> frequencyBuffers = new ConcurrentHashMap<>();
    private final Map<String, LiveFeedBurstDetector> burstDetectors = new ConcurrentHashMap<>();
    private final Map<String, Integer> captureCounts = new ConcurrentHashMap<>();
    private final LiveFeedDispatchProcessor dispatchProcessor;
    private long lastStatusTickMillis;

    public TraceLiveFeedService(
            TraceSessionManager traceSessionManager, LiveFeedDeliveryPort deliveryPort, LiveFeedConfig liveFeedConfig) {
        this.traceSessionManager = traceSessionManager;
        this.deliveryPort = deliveryPort;
        this.dispatchProcessor = new LiveFeedDispatchProcessor(
                deliveryPort, liveFeedConfig, frequencyBuffers, burstDetectors, captureCounts);
    }

    public LiveFeedCommandResult subscribe(
            String viewerName, UUID viewerId, String sessionId, LiveFeedSettings settings) {
        if (traceSessionManager.getSessionDetail(sessionId).isEmpty()) {
            return new LiveFeedCommandResult.NotFound(sessionId);
        }
        if (subscriptions.size() >= LiveFeedLimits.MAX_SUBSCRIBERS && !subscriptions.containsKey(viewerName)) {
            return new LiveFeedCommandResult.Failure("Live feed subscriber limit reached.");
        }

        long nowMillis = System.currentTimeMillis();
        LiveFeedSubscription existing = subscriptions.get(viewerName);
        if (existing != null && existing.sessionId().equals(sessionId)) {
            LiveFeedSubscription updated = new LiveFeedSubscription(
                    viewerName,
                    viewerId,
                    sessionId,
                    settings,
                    existing.capturedEvents(),
                    existing.subscribedAtMillis());
            subscriptions.put(viewerName, updated);
            return new LiveFeedCommandResult.Updated(updated);
        }

        LiveFeedSubscription subscription =
                new LiveFeedSubscription(viewerName, viewerId, sessionId, settings, 0, nowMillis);
        subscriptions.put(viewerName, subscription);
        frequencyBuffers.put(viewerName, new LiveFeedFrequencyBuffer());
        burstDetectors.put(viewerName, new LiveFeedBurstDetector());
        captureCounts.put(viewerName, 0);
        return new LiveFeedCommandResult.Subscribed(subscription);
    }

    public LiveFeedCommandResult unsubscribe(String viewerName) {
        LiveFeedSubscription removed = subscriptions.remove(viewerName);
        frequencyBuffers.remove(viewerName);
        burstDetectors.remove(viewerName);
        captureCounts.remove(viewerName);
        if (removed == null) {
            return new LiveFeedCommandResult.NoSubscription();
        }
        deliveryPort.clearStatus(removed.viewerId());
        return new LiveFeedCommandResult.Unsubscribed(removed.sessionId());
    }

    public LiveFeedCommandResult pause(String viewerName, boolean paused) {
        LiveFeedSubscription subscription = subscriptions.get(viewerName);
        if (subscription == null) {
            return new LiveFeedCommandResult.NoSubscription();
        }
        LiveFeedSubscription updated = new LiveFeedSubscription(
                subscription.viewerName(),
                subscription.viewerId(),
                subscription.sessionId(),
                subscription.settings().withPaused(paused),
                subscription.capturedEvents(),
                subscription.subscribedAtMillis());
        subscriptions.put(viewerName, updated);
        return new LiveFeedCommandResult.Updated(updated);
    }

    public LiveFeedCommandResult status(String viewerName) {
        LiveFeedSubscription subscription = subscriptions.get(viewerName);
        if (subscription == null) {
            return new LiveFeedCommandResult.NoSubscription();
        }
        return new LiveFeedCommandResult.Status(subscription);
    }

    @Override
    public void onDispatchCaptured(String sessionId, TraceDispatchRecord dispatch) {
        traceSessionManager.getSessionConfig(sessionId).ifPresent(config -> {
            for (LiveFeedSubscription subscription : subscriptions.values()) {
                if (subscription.sessionId().equals(sessionId)) {
                    dispatchProcessor.processDispatch(subscription, dispatch, config, dispatch.startedAtMillis());
                }
            }
        });
    }

    public void tick(long nowMillis) {
        if (nowMillis - lastStatusTickMillis >= LiveFeedLimits.STATUS_UPDATE_INTERVAL_MILLIS) {
            lastStatusTickMillis = nowMillis;
            dispatchProcessor.updateStatusBars(subscriptions.values());
        }
        dispatchProcessor.flushFrequencyBuffers(subscriptions.values(), nowMillis);
    }

    @Override
    public void onSessionStopped(String sessionId) {
        List<String> toRemove = new ArrayList<>();
        for (LiveFeedSubscription subscription : subscriptions.values()) {
            if (subscription.sessionId().equals(sessionId)) {
                deliveryPort.clearStatus(subscription.viewerId());
                toRemove.add(subscription.viewerName());
            }
        }
        toRemove.forEach(this::unsubscribe);
    }

    public void onViewerDisconnect(String viewerName) {
        unsubscribe(viewerName);
    }
}
