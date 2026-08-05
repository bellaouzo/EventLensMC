package dev.bellaouzo.eventlens.domain.live;

import java.util.Locale;

public enum LiveFeedDisplayMode {
    CHAT,
    ACTION_BAR,
    BOSS_BAR;

    public static LiveFeedDisplayMode parse(String value) {
        if (value == null || value.isBlank()) {
            return CHAT;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "actionbar", "action-bar", "action_bar" -> ACTION_BAR;
            case "bossbar", "boss-bar", "boss_bar" -> BOSS_BAR;
            default -> CHAT;
        };
    }
}
