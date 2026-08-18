package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ModDispatchSummary {

    private static final Set<String> VALUE_ONLY =
            Set.of("message", "screen", "item", "target", "sound", "toast", "file", "entity", "dimension");
    private static final Set<String> PREFERRED = Set.of(
            "message",
            "screen",
            "item",
            "target",
            "sound",
            "toast",
            "file",
            "entity",
            "dimension",
            "key",
            "action",
            "button",
            "hand",
            "from",
            "to",
            "paused");
    private static final int MAX_PREVIEWS = 2;
    private static final int MAX_VALUE = 28;

    private ModDispatchSummary() {}

    public static String listLine(TraceDispatchRecord record) {
        StringBuilder text = new StringBuilder();
        text.append('#').append(record.sequence());
        text.append("  ").append(String.format(Locale.ROOT, "%.2f ms", record.durationNanos() / 1_000_000.0));
        if (record.cancelledAtEnd()) {
            text.append("  cancelled");
        }
        record.playerName().filter(name -> !name.isBlank()).ifPresent(name -> text.append("  ·  ").append(name));
        for (String preview : previews(record)) {
            text.append("  ·  ").append(preview);
        }
        int handlers = handlerCount(record);
        if (handlers == 1) {
            text.append("  ·  1 handler");
        } else if (handlers > 1) {
            text.append("  ·  ").append(handlers).append(" handlers");
        }
        if (record.correlation().linked()) {
            text.append("  ·  linked");
        }
        return text.toString();
    }

    private static int handlerCount(TraceDispatchRecord record) {
        List<ListenerTimingRecord> timings = record.listenerTimings();
        return timings == null ? 0 : timings.size();
    }

    private static List<String> previews(TraceDispatchRecord record) {
        EventSnapshot snapshot = record.snapshotAfter();
        List<SnapshotField> fields = snapshot == null ? List.of() : snapshot.fields();
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }
        List<String> preferred = new ArrayList<>();
        List<String> fallback = new ArrayList<>();
        for (SnapshotField field : fields) {
            if ("cancelled".equals(field.name())) {
                continue;
            }
            String piece = formatField(field);
            if (piece.isBlank()) {
                continue;
            }
            if (PREFERRED.contains(field.name())) {
                preferred.add(piece);
            } else {
                fallback.add(piece);
            }
        }
        List<String> chosen = preferred.isEmpty() ? fallback : preferred;
        if (chosen.size() <= MAX_PREVIEWS) {
            return chosen;
        }
        return List.copyOf(chosen.subList(0, MAX_PREVIEWS));
    }

    private static String formatField(SnapshotField field) {
        String value = display(field.value());
        if (value.isBlank()) {
            return "";
        }
        if (value.length() > MAX_VALUE) {
            value = value.substring(0, MAX_VALUE - 3) + "...";
        }
        if (VALUE_ONLY.contains(field.name())) {
            return value;
        }
        return field.name() + " " + value;
    }

    private static String display(SnapshotValue value) {
        return switch (value) {
            case SnapshotValue.Present present -> present.display();
            case SnapshotValue.Truncated truncated -> truncated.display();
            case SnapshotValue.Unsupported ignored -> "";
        };
    }
}
