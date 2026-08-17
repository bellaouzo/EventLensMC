package dev.bellaouzo.eventlens.trace;

public interface SessionLifecycleListener {

    default void onSessionStarted(String sessionId) {}

    void onSessionStopped(String sessionId);
}
