package dev.bellaouzo.eventlens.command;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import org.jspecify.annotations.Nullable;

public final class DetailLevelParser {

    private static final String DETAIL_FLAG = "--detail";

    private DetailLevelParser() {}

    public static OutputDetailLevel resolve(String[] args, int startIndex, EventLensCommandConfig commandConfig) {
        for (int index = startIndex; index < args.length; index++) {
            if (args[index].equalsIgnoreCase(DETAIL_FLAG) && index + 1 < args.length) {
                return OutputDetailLevel.parse(args[index + 1]);
            }
        }
        return commandConfig.defaultDetailLevel();
    }

    public static PageArgs parseListenersArgs(String[] args, EventLensCommandConfig commandConfig) {
        int page = 1;
        OutputDetailLevel detailLevel = commandConfig.defaultDetailLevel();
        int index = 2;
        while (index < args.length) {
            String token = args[index];
            if (token.equalsIgnoreCase(DETAIL_FLAG) && index + 1 < args.length) {
                detailLevel = OutputDetailLevel.parse(args[index + 1]);
                index += 2;
            } else if (page == 1) {
                page = parsePage(token);
                index++;
            } else {
                index++;
            }
        }
        return new PageArgs(page, detailLevel, false);
    }

    public static PluginListenersPageArgs parsePluginListenersArgs(
            String[] args, EventLensCommandConfig commandConfig) {
        int page = 1;
        OutputDetailLevel detailLevel = commandConfig.defaultDetailLevel();
        String eventQuery = null;
        int index = 3;
        while (index < args.length) {
            String token = args[index];
            if (token.equalsIgnoreCase(DETAIL_FLAG) && index + 1 < args.length) {
                detailLevel = OutputDetailLevel.parse(args[index + 1]);
                index += 2;
            } else if (eventQuery == null && !token.startsWith("--")) {
                if (looksLikePage(token)) {
                    page = parsePage(token);
                } else {
                    eventQuery = token;
                }
                index++;
            } else if (!token.startsWith("--")) {
                page = parsePage(token);
                index++;
            } else {
                index++;
            }
        }
        return new PluginListenersPageArgs(page, detailLevel, eventQuery);
    }

    public static PageArgs parseViewArgs(String[] args, EventLensCommandConfig commandConfig) {
        int page = 1;
        boolean includeUnchanged = false;
        OutputDetailLevel detailLevel = commandConfig.defaultDetailLevel();
        int index = 3;
        while (index < args.length) {
            String token = args[index];
            if (token.equalsIgnoreCase("--unchanged")) {
                includeUnchanged = true;
                index++;
            } else if (token.equalsIgnoreCase(DETAIL_FLAG) && index + 1 < args.length) {
                detailLevel = OutputDetailLevel.parse(args[index + 1]);
                index += 2;
            } else if (page == 1) {
                page = parsePage(token);
                index++;
            } else {
                index++;
            }
        }
        return new PageArgs(page, detailLevel, includeUnchanged);
    }

    public static int parsePage(String token) {
        try {
            int page = Integer.parseInt(token);
            if (page < 1) {
                throw new IllegalArgumentException("Page must be a positive integer.");
            }
            return page;
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("Page must be a positive integer.");
        }
    }

    private static boolean looksLikePage(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException _) {
            return false;
        }
    }

    public record PageArgs(int page, OutputDetailLevel detailLevel, boolean includeUnchanged) {}

    public record PluginListenersPageArgs(int page, OutputDetailLevel detailLevel, @Nullable String eventQuery) {}
}
