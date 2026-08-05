package dev.bellaouzo.eventlens.domain.live;

public sealed interface LiveFeedCommandResult {

    record Subscribed(LiveFeedSubscription subscription) implements LiveFeedCommandResult {}

    record Updated(LiveFeedSubscription subscription) implements LiveFeedCommandResult {}

    record Unsubscribed(String sessionId) implements LiveFeedCommandResult {}

    record Status(LiveFeedSubscription subscription) implements LiveFeedCommandResult {}

    record NotFound(String sessionId) implements LiveFeedCommandResult {}

    record NoSubscription() implements LiveFeedCommandResult {}

    record Failure(String message) implements LiveFeedCommandResult {}

    record SessionEnded(String sessionId) implements LiveFeedCommandResult {}
}
