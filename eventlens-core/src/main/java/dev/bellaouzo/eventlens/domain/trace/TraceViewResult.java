package dev.bellaouzo.eventlens.domain.trace;

import dev.bellaouzo.eventlens.application.DispatchViewFilter;

public sealed interface TraceViewResult
        permits TraceViewResult.Success, TraceViewResult.NotFound, TraceViewResult.InvalidPage {

    record Success(
            TraceSessionDetail detail,
            int page,
            int totalPages,
            boolean includeUnchanged,
            DispatchViewFilter filter,
            int totalMatchedDispatches,
            int totalSessionDispatches)
            implements TraceViewResult {}

    record NotFound(String sessionId) implements TraceViewResult {}

    record InvalidPage(int requestedPage, int totalPages) implements TraceViewResult {}
}
