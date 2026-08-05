package dev.bellaouzo.eventlens.trace;

public interface SessionLifecycleListener {

    void onSessionStopped(String sessionId);
}
