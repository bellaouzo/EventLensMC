package dev.bellaouzo.eventlens.domain.trace;

import java.util.List;

public sealed interface TraceStopResult
        permits TraceStopResult.Success, TraceStopResult.NoActiveSessions, TraceStopResult.NotFound {

    record Success(List<String> stoppedSessionIds) implements TraceStopResult {}

    record NoActiveSessions() implements TraceStopResult {}

    record NotFound(String sessionId) implements TraceStopResult {}
}
