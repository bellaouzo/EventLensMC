package dev.bellaouzo.eventlens.domain.dashboard;

import org.jspecify.annotations.NonNull;

public record DashboardStatusPayload(
        boolean dashboardEnabled,
        int port,
        @NonNull String bindAddress,
        boolean agentPresent,
        int protocolVersion,
        @NonNull DashboardServerContext server,
        @NonNull String activeTraceSessionId,
        long activeTraceStartedAtMillis,
        int activeTraceCapturedEvents,
        @NonNull String activeTraceEventClassName) {}
