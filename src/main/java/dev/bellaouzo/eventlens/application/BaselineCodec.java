package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.report.TraceRegressionData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class BaselineCodec {

    private static final String BASELINE_SCHEMA = "eventlens-baseline-v1";

    private BaselineCodec() {}

    static String encode(String baselineName, TraceRegressionData data) {
        StringBuilder out = new StringBuilder();
        out.append("schema=").append(BASELINE_SCHEMA).append('\n');
        out.append("name=").append(baselineName).append('\n');
        out.append("sourceId=").append(data.sourceId()).append('\n');
        out.append("eventClass=").append(data.eventClassName()).append('\n');
        out.append("state=").append(data.sessionState()).append('\n');
        out.append("captured=").append(data.capturedDispatches()).append('\n');
        out.append("dropped=").append(data.droppedDispatches()).append('\n');
        out.append("filter=").append(escape(data.filterDescription())).append('\n');
        out.append("warningCount=").append(data.warningCount()).append('\n');
        out.append("dispatchCount=").append(data.dispatchCount()).append('\n');
        out.append("totalDurationNanos=").append(data.totalDurationNanos()).append('\n');
        out.append("cancelledAtEndCount=").append(data.cancelledAtEndCount()).append('\n');
        out.append("partialDispatchCount=").append(data.partialDispatchCount()).append('\n');
        out.append("pluginDispatchCounts=")
                .append(encodeMap(data.pluginDispatchCounts()))
                .append('\n');
        out.append("pluginInvocationCounts=")
                .append(encodeMap(data.pluginInvocationCounts()))
                .append('\n');
        out.append("dispatchFingerprints=")
                .append(encodeList(data.dispatchFingerprints()))
                .append('\n');
        return out.toString();
    }

    static Optional<TraceRegressionData> decode(String content, Optional<String> pluginScope) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String line : content.split("\\R")) {
            int index = line.indexOf('=');
            if (index <= 0) {
                continue;
            }
            fields.put(line.substring(0, index), line.substring(index + 1));
        }
        if (!BASELINE_SCHEMA.equals(fields.get("schema"))) {
            return Optional.empty();
        }
        try {
            String sourceId = fields.getOrDefault("sourceId", fields.getOrDefault("name", "baseline"));
            String scopedSource = pluginScope
                    .map(plugin -> sourceId + " (plugin=" + plugin + ")")
                    .orElse(sourceId);
            return Optional.of(new TraceRegressionData(
                    scopedSource,
                    fields.getOrDefault("eventClass", ""),
                    fields.getOrDefault("state", ""),
                    Integer.parseInt(fields.getOrDefault("captured", "0")),
                    Integer.parseInt(fields.getOrDefault("dropped", "0")),
                    unescape(fields.getOrDefault("filter", "")),
                    Integer.parseInt(fields.getOrDefault("warningCount", "0")),
                    Integer.parseInt(fields.getOrDefault("dispatchCount", "0")),
                    Long.parseLong(fields.getOrDefault("totalDurationNanos", "0")),
                    Integer.parseInt(fields.getOrDefault("cancelledAtEndCount", "0")),
                    Integer.parseInt(fields.getOrDefault("partialDispatchCount", "0")),
                    decodeMap(fields.getOrDefault("pluginDispatchCounts", "")),
                    decodeMap(fields.getOrDefault("pluginInvocationCounts", "")),
                    decodeList(fields.getOrDefault("dispatchFingerprints", ""))));
        } catch (RuntimeException _) {
            return Optional.empty();
        }
    }

    private static String encodeMap(Map<String, Integer> values) {
        if (values.isEmpty()) {
            return "";
        }
        return values.entrySet().stream()
                .map(entry -> escape(entry.getKey()) + ":" + entry.getValue())
                .reduce((left, right) -> left + ";" + right)
                .orElse("");
    }

    private static Map<String, Integer> decodeMap(String encoded) {
        if (encoded.isBlank()) {
            return Map.of();
        }
        Map<String, Integer> parsed = new LinkedHashMap<>();
        for (String part : encoded.split(";")) {
            int separator = part.lastIndexOf(':');
            if (separator <= 0 || separator == part.length() - 1) {
                continue;
            }
            parsed.put(unescape(part.substring(0, separator)), Integer.parseInt(part.substring(separator + 1)));
        }
        return parsed;
    }

    private static String encodeList(List<String> values) {
        if (values.isEmpty()) {
            return "";
        }
        return values.stream()
                .map(BaselineCodec::escape)
                .reduce((left, right) -> left + ";" + right)
                .orElse("");
    }

    private static List<String> decodeList(String encoded) {
        if (encoded.isBlank()) {
            return List.of();
        }
        List<String> parsed = new ArrayList<>();
        for (String part : encoded.split(";")) {
            parsed.add(unescape(part));
        }
        return List.copyOf(parsed);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace(";", "\\s")
                .replace(":", "\\c");
    }

    private static String unescape(String value) {
        return value.replace("\\c", ":")
                .replace("\\s", ";")
                .replace("\\n", "\n")
                .replace("\\\\", "\\");
    }
}
