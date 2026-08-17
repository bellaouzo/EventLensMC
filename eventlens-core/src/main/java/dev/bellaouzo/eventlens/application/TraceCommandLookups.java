package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;

final class TraceCommandLookups {

    private TraceCommandLookups() {}

    static List<String> sequenceTokens(TraceSessionManager manager, String sessionId) {
        return manager.getSessionDetail(sessionId)
                .map(detail -> detail.records().stream()
                        .map(dispatchRecord -> Long.toString(dispatchRecord.sequence()))
                        .toList())
                .orElse(List.of());
    }

    static List<String> pluginNames(TraceSessionManager manager, String sessionId) {
        return manager.getSessionDetail(sessionId)
                .map(detail -> detail.records().stream()
                        .flatMap(dispatchRecord -> dispatchRecord.listenerChain().stream())
                        .map(TraceListenerSnapshot::pluginName)
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList())
                .orElse(List.of());
    }
}
