package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.live.LiveFeedChannel;
import dev.bellaouzo.eventlens.domain.live.LiveFeedDisplayMode;
import dev.bellaouzo.eventlens.domain.live.LiveFeedSettings;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class TraceLiveOptionsParser {

    private static final String FLAG_CHANNELS = "--channels";
    private static final String FLAG_DISPLAY = "--display";
    private static final String FLAG_FILTER_PLUGIN = "--filter-plugin";
    private static final String FLAG_THRESHOLD = "--threshold";
    private static final String FLAG_BURST = "--burst";
    private static final String FLAG_AGGREGATE = "--aggregate";
    private static final String FLAG_PAUSE = "--pause";
    private static final String FLAG_RESUME = "--resume";
    private static final String FLAG_STOP = "--stop";

    private TraceLiveOptionsParser() {}

    public static ParsedLiveOptions parse(
            String[] args, LiveFeedConfig liveFeedConfig, long defaultSlowThresholdNanos) {
        OptionsBuilder builder = new OptionsBuilder(liveFeedConfig, defaultSlowThresholdNanos);
        int index = 2;
        while (index < args.length) {
            index = builder.consume(args, index) + 1;
        }
        return builder.build();
    }

    public static List<String> channelSuggestions() {
        return List.of("frequency", "slow", "cancel", "exception", "alert");
    }

    public static List<String> displaySuggestions() {
        return List.of("chat", "actionbar", "bossbar");
    }

    public static List<String> flagSuggestions() {
        return List.of(FLAG_CHANNELS, FLAG_DISPLAY, FLAG_FILTER_PLUGIN, FLAG_THRESHOLD, FLAG_BURST, FLAG_AGGREGATE);
    }

    private static final class OptionsBuilder {
        private String sessionId;
        private boolean stop;
        private Boolean pause;
        private EnumSet<LiveFeedChannel> channels;
        private LiveFeedDisplayMode displayMode;
        private Optional<String> pluginFilter = Optional.empty();
        private long slowThreshold;
        private int burstThreshold;
        private long aggregateWindow;
        private final long burstWindow;

        private OptionsBuilder(LiveFeedConfig liveFeedConfig, long defaultSlowThresholdNanos) {
            channels = EnumSet.allOf(LiveFeedChannel.class);
            displayMode = liveFeedConfig.defaultStatusDisplay();
            slowThreshold = defaultSlowThresholdNanos;
            burstThreshold = liveFeedConfig.burstThreshold();
            aggregateWindow = liveFeedConfig.aggregateWindowMillis();
            burstWindow = liveFeedConfig.burstWindowMillis();
        }

        int consume(String[] args, int index) {
            String token = args[index];
            if (applyControlFlag(token)) {
                return index;
            }
            Integer valueIndex = applyValueFlag(args, index, token);
            if (valueIndex != null) {
                return valueIndex;
            }
            if (!token.startsWith("--") && sessionId == null) {
                sessionId = token;
            }
            return index;
        }

        private boolean applyControlFlag(String token) {
            if (token.equalsIgnoreCase(FLAG_STOP)) {
                stop = true;
                return true;
            }
            if (token.equalsIgnoreCase(FLAG_PAUSE)) {
                pause = true;
                return true;
            }
            if (token.equalsIgnoreCase(FLAG_RESUME)) {
                pause = false;
                return true;
            }
            return false;
        }

        private Integer applyValueFlag(String[] args, int index, String token) {
            if (index + 1 >= args.length) {
                return null;
            }
            if (token.equalsIgnoreCase(FLAG_CHANNELS)) {
                channels = parseChannels(args[index + 1]);
                return index + 1;
            }
            if (token.equalsIgnoreCase(FLAG_DISPLAY)) {
                displayMode = LiveFeedDisplayMode.parse(args[index + 1]);
                return index + 1;
            }
            if (token.equalsIgnoreCase(FLAG_FILTER_PLUGIN)) {
                pluginFilter = Optional.of(args[index + 1]);
                return index + 1;
            }
            if (token.equalsIgnoreCase(FLAG_THRESHOLD)) {
                slowThreshold = parseThresholdNanos(args[index + 1], slowThreshold);
                return index + 1;
            }
            if (token.equalsIgnoreCase(FLAG_BURST)) {
                burstThreshold = Integer.parseInt(args[index + 1]);
                return index + 1;
            }
            if (token.equalsIgnoreCase(FLAG_AGGREGATE)) {
                aggregateWindow = parseDurationMillis(args[index + 1], aggregateWindow);
                return index + 1;
            }
            return null;
        }

        ParsedLiveOptions build() {
            LiveFeedSettings settings = new LiveFeedSettings(
                    channels,
                    displayMode,
                    pause != null && pause,
                    pluginFilter,
                    slowThreshold,
                    burstThreshold,
                    burstWindow,
                    aggregateWindow);
            return new ParsedLiveOptions(sessionId, stop, pause, settings);
        }

        private static EnumSet<LiveFeedChannel> parseChannels(String value) {
            EnumSet<LiveFeedChannel> parsed = EnumSet.noneOf(LiveFeedChannel.class);
            for (String part : value.split(",")) {
                switch (part.trim().toLowerCase(Locale.ROOT)) {
                    case "frequency", "freq" -> parsed.add(LiveFeedChannel.FREQUENCY);
                    case "slow" -> parsed.add(LiveFeedChannel.SLOW);
                    case "cancel", "cancellation" -> parsed.add(LiveFeedChannel.CANCELLATION);
                    case "exception", "exceptions" -> parsed.add(LiveFeedChannel.EXCEPTION);
                    case "alert", "alerts" -> parsed.add(LiveFeedChannel.ALERT);
                    default -> {
                        /* ignore unknown channel token */
                    }
                }
            }
            return parsed.isEmpty() ? EnumSet.allOf(LiveFeedChannel.class) : parsed;
        }

        private static long parseThresholdNanos(String value, long fallback) {
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            if (normalized.endsWith("ms")) {
                return Math.max(
                        1L,
                        Math.round(Double.parseDouble(normalized.substring(0, normalized.length() - 2)) * 1_000_000L));
            }
            if (normalized.endsWith("ns")) {
                return Math.max(1L, Long.parseLong(normalized.substring(0, normalized.length() - 2)));
            }
            try {
                return Math.max(1L, Math.round(Double.parseDouble(normalized) * 1_000_000L));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }

        private static long parseDurationMillis(String value, long fallback) {
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            if (normalized.endsWith("ms")) {
                return Math.max(500L, Long.parseLong(normalized.substring(0, normalized.length() - 2)));
            }
            if (normalized.endsWith("s")) {
                return Math.max(500L, Long.parseLong(normalized.substring(0, normalized.length() - 1)) * 1000L);
            }
            try {
                return Math.max(500L, Long.parseLong(normalized));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
    }

    public record ParsedLiveOptions(String sessionId, boolean stop, Boolean pauseOverride, LiveFeedSettings settings) {}
}
