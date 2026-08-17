package dev.bellaouzo.eventlens.modcommon.chat;

public record ModChatSpan(
        String text,
        ModChatColor color,
        boolean bold,
        boolean underline,
        String clickValue,
        String hoverText,
        ModChatClick clickKind) {

    public static ModChatSpan of(String text, ModChatColor color) {
        return new ModChatSpan(text, color, false, false, "", "", ModChatClick.NONE);
    }

    public static ModChatSpan titled(String text, ModChatColor color, String hoverText) {
        return new ModChatSpan(text, color, true, false, "", hoverText, ModChatClick.NONE);
    }

    public static ModChatSpan click(String text, ModChatColor color, String command, String hoverText) {
        return new ModChatSpan(text, color, false, true, command, hoverText, ModChatClick.RUN_COMMAND);
    }

    public static ModChatSpan copy(String text, ModChatColor color, String value, String hoverText) {
        return new ModChatSpan(text, color, false, true, value, hoverText, ModChatClick.COPY);
    }

    public String clickCommand() {
        return clickKind == ModChatClick.RUN_COMMAND ? clickValue : "";
    }
}
