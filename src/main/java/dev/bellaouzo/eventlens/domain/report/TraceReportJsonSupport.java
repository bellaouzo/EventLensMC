package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.observability.RankedListenerTiming;
import dev.bellaouzo.eventlens.domain.observability.RankedPluginTiming;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import java.util.List;
import java.util.Optional;

final class TraceReportJsonSupport {

    private TraceReportJsonSupport() {}

    @SuppressWarnings("java:S135")
    static String minifyJson(String json) {
        StringBuilder minified = new StringBuilder(json.length());
        boolean inString = false;
        boolean escaped = false;
        for (int index = 0; index < json.length(); index++) {
            char character = json.charAt(index);
            if (inString) {
                minified.append(character);
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    inString = false;
                }
                continue;
            }
            if (Character.isWhitespace(character)) {
                continue;
            }
            minified.append(character);
            if (character == '"') {
                inString = true;
            }
        }
        return minified.toString();
    }

    static void appendInstrumentation(StringBuilder json, TraceReportInstrumentation instrumentation, int depth) {
        json.append('{');
        TraceReportJsonSerializer.fieldString(
                json, depth + 1, "mode", instrumentation.mode().name());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "agentPresent", instrumentation.agentPresent());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "protocolVersion", instrumentation.protocolVersion());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(
                json, depth + 1, "protocolCompatible", instrumentation.protocolCompatible());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(
                json, depth + 1, "paperVersionCompatible", instrumentation.paperVersionCompatible());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "capabilities");
        json.append('{');
        TraceReportJsonSerializer.fieldBoolean(
                json,
                depth + 2,
                "perListenerDuration",
                instrumentation.capabilities().perListenerDuration());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(
                json,
                depth + 2,
                "perListenerSnapshots",
                instrumentation.capabilities().perListenerSnapshots());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(
                json,
                depth + 2,
                "perListenerPropertyDiffs",
                instrumentation.capabilities().perListenerPropertyDiffs());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(
                json,
                depth + 2,
                "exactCancellationTimeline",
                instrumentation.capabilities().exactCancellationTimeline());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(
                json,
                depth + 2,
                "priorityBandFallback",
                instrumentation.capabilities().priorityBandFallback());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(
                json,
                depth + 2,
                "exceptionPreservation",
                instrumentation.capabilities().exceptionPreservation());
        TraceReportJsonSerializer.indent(json, depth + 1);
        json.append('}');
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    static void appendOptionalSessionTimingSummary(
            StringBuilder json, Optional<SessionTimingSummary> summary, int depth) {
        if (summary.isEmpty()) {
            json.append("null");
            return;
        }
        appendSessionTimingSummary(json, summary.get(), depth);
    }

    static void appendSessionTimingSummary(StringBuilder json, SessionTimingSummary summary, int depth) {
        json.append('{');
        TraceReportJsonSerializer.key(json, depth + 1, "dispatchStats");
        appendDurationStats(json, summary.dispatchStats(), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "eventLensOverheadStats");
        appendDurationStats(json, summary.eventLensOverheadStats(), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "slowestListeners");
        appendRankedListenerTimings(json, summary.slowestListeners(), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "slowestPlugins");
        appendRankedPluginTimings(json, summary.slowestPlugins(), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "frequentListenerWarnings");
        TraceReportJsonSerializer.appendStringList(json, summary.frequentListenerWarnings(), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "sampledOutEvents", summary.sampledOutEvents());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.key(json, depth + 1, "sessionPartialReasons");
        TraceReportJsonSerializer.appendEnumList(json, summary.sessionPartialReasons(), depth + 1);
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "slowThresholdNanos", summary.slowThresholdNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldBoolean(json, depth + 1, "agentAttached", summary.agentAttached());
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendDurationStats(StringBuilder json, DurationStats stats, int depth) {
        json.append('{');
        TraceReportJsonSerializer.fieldInt(json, depth + 1, "count", stats.count());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "minNanos", stats.minNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "maxNanos", stats.maxNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "averageNanos", stats.averageNanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "p50Nanos", stats.p50Nanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "p95Nanos", stats.p95Nanos());
        TraceReportJsonSerializer.comma(json);
        TraceReportJsonSerializer.fieldLong(json, depth + 1, "p99Nanos", stats.p99Nanos());
        TraceReportJsonSerializer.indent(json, depth);
        json.append('}');
    }

    private static void appendRankedListenerTimings(StringBuilder json, List<RankedListenerTiming> timings, int depth) {
        json.append('[');
        for (int index = 0; index < timings.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            RankedListenerTiming timing = timings.get(index);
            json.append('\n').append("  ".repeat(depth + 1)).append('{');
            TraceReportJsonSerializer.fieldString(
                    json, depth + 2, "pluginName", timing.identity().pluginName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(
                    json, depth + 2, "listenerClassName", timing.identity().listenerClassName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldString(
                    json, depth + 2, "methodName", timing.identity().methodName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldInt(json, depth + 2, "invocationCount", timing.invocationCount());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldBoolean(json, depth + 2, "frequentlyInvoked", timing.frequentlyInvoked());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldBoolean(json, depth + 2, "mainThreadBlocked", timing.mainThreadBlocked());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.key(json, depth + 2, "stats");
            appendDurationStats(json, timing.stats(), depth + 2);
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        if (!timings.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }

    private static void appendRankedPluginTimings(StringBuilder json, List<RankedPluginTiming> timings, int depth) {
        json.append('[');
        for (int index = 0; index < timings.size(); index++) {
            if (index > 0) {
                TraceReportJsonSerializer.comma(json);
            }
            RankedPluginTiming timing = timings.get(index);
            json.append('\n').append("  ".repeat(depth + 1)).append('{');
            TraceReportJsonSerializer.fieldString(json, depth + 2, "pluginName", timing.pluginName());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.fieldInt(json, depth + 2, "invocationCount", timing.invocationCount());
            TraceReportJsonSerializer.comma(json);
            TraceReportJsonSerializer.key(json, depth + 2, "stats");
            appendDurationStats(json, timing.stats(), depth + 2);
            TraceReportJsonSerializer.indent(json, depth + 1);
            json.append('}');
        }
        if (!timings.isEmpty()) {
            TraceReportJsonSerializer.indent(json, depth);
        }
        json.append(']');
    }
}
