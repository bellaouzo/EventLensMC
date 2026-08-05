package dev.bellaouzo.eventlens.domain.trace;

import java.util.List;

public sealed interface TraceStopResult permits TraceStopResult.Success, TraceStopResult.NoActiveSessions {

    record Success(List<String> stoppedSessionIds) implements TraceStopResult {}

    record NoActiveSessions() implements TraceStopResult {}
}
