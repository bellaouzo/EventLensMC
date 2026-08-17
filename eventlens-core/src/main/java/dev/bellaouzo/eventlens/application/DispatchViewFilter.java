package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class DispatchViewFilter {

    private final Optional<Long> dispatchSequence;
    private final Optional<String> pluginName;
    private final Optional<String> pluginNameNormalized;
    private final boolean changedOnly;
    private final boolean slowOnly;
    private final boolean conflictOnly;

    private DispatchViewFilter(
            Optional<Long> dispatchSequence,
            Optional<String> pluginName,
            boolean changedOnly,
            boolean slowOnly,
            boolean conflictOnly) {
        this.dispatchSequence = dispatchSequence;
        this.pluginName = pluginName;
        this.pluginNameNormalized = pluginName.map(DispatchViewFilter::normalizeName);
        this.changedOnly = changedOnly;
        this.slowOnly = slowOnly;
        this.conflictOnly = conflictOnly;
    }

    public static DispatchViewFilter unrestricted() {
        return new Builder().build();
    }

    public Optional<Long> dispatchSequence() {
        return dispatchSequence;
    }

    public Optional<String> pluginName() {
        return pluginName;
    }

    public boolean changedOnly() {
        return changedOnly;
    }

    public boolean slowOnly() {
        return slowOnly;
    }

    public boolean conflictOnly() {
        return conflictOnly;
    }

    public boolean hasPredicates() {
        return dispatchSequence.isPresent() || pluginName.isPresent() || changedOnly || slowOnly || conflictOnly;
    }

    public boolean matches(TraceDispatchRecord dispatchRecord, long slowThresholdNanos) {
        return (!dispatchSequence.isPresent() || dispatchRecord.sequence() == dispatchSequence.get())
                && (!pluginNameNormalized.isPresent() || containsPlugin(dispatchRecord, pluginNameNormalized.get()))
                && (!changedOnly || hasMeaningfulChange(dispatchRecord))
                && (!slowOnly || isSlowDispatch(dispatchRecord, slowThresholdNanos))
                && (!conflictOnly
                        || !SessionConflictAnalyzer.detectForDispatch(dispatchRecord, slowThresholdNanos)
                                .isEmpty());
    }

    public List<String> toCommandTokens() {
        List<String> tokens = new ArrayList<>();
        dispatchSequence.ifPresent(sequence -> {
            tokens.add("--dispatch");
            tokens.add(Long.toString(sequence));
        });
        pluginName.ifPresent(name -> {
            tokens.add("--plugin");
            tokens.add(name);
        });
        if (changedOnly) {
            tokens.add("--changed");
        }
        if (slowOnly) {
            tokens.add("--slow");
        }
        if (conflictOnly) {
            tokens.add("--conflict");
        }
        return List.copyOf(tokens);
    }

    private static boolean containsPlugin(TraceDispatchRecord dispatchRecord, String pluginNameNormalized) {
        for (var listener : dispatchRecord.listenerChain()) {
            if (normalizeName(listener.pluginName()).equals(pluginNameNormalized)) {
                return true;
            }
        }
        for (ListenerTimingRecord timing : dispatchRecord.listenerTimings()) {
            if (normalizeName(timing.pluginName()).equals(pluginNameNormalized)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasMeaningfulChange(TraceDispatchRecord dispatchRecord) {
        var diff = TraceDispatchAnalyzer.overallDiff(dispatchRecord, false);
        return !diff.changed().isEmpty()
                || diff.cancellationTransition()
                        .map(transition -> transition.kind() != CancellationTransitionKind.UNCHANGED)
                        .orElse(false);
    }

    private static boolean isSlowDispatch(TraceDispatchRecord dispatchRecord, long slowThresholdNanos) {
        if (dispatchRecord.durationNanos() >= slowThresholdNanos) {
            return true;
        }
        for (ListenerTimingRecord timing : dispatchRecord.listenerTimings()) {
            if (timing.durationNanos() >= slowThresholdNanos || timing.exceedsSlowThreshold()) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeName(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static final class Builder {
        private Optional<Long> dispatchSequence = Optional.empty();
        private Optional<String> pluginName = Optional.empty();
        private boolean changedOnly;
        private boolean slowOnly;
        private boolean conflictOnly;

        public Builder dispatchSequence(long dispatchSequence) {
            if (dispatchSequence < 1L) {
                throw new IllegalArgumentException("Dispatch sequence must be positive.");
            }
            this.dispatchSequence = Optional.of(dispatchSequence);
            return this;
        }

        public Builder pluginName(String pluginName) {
            Objects.requireNonNull(pluginName, "pluginName");
            String trimmed = pluginName.trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Plugin name cannot be blank.");
            }
            this.pluginName = Optional.of(trimmed);
            return this;
        }

        public Builder changedOnly(boolean changedOnly) {
            this.changedOnly = changedOnly;
            return this;
        }

        public Builder slowOnly(boolean slowOnly) {
            this.slowOnly = slowOnly;
            return this;
        }

        public Builder conflictOnly(boolean conflictOnly) {
            this.conflictOnly = conflictOnly;
            return this;
        }

        public DispatchViewFilter build() {
            return new DispatchViewFilter(dispatchSequence, pluginName, changedOnly, slowOnly, conflictOnly);
        }
    }
}
