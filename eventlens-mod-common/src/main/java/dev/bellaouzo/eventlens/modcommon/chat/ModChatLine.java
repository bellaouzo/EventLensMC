package dev.bellaouzo.eventlens.modcommon.chat;

import java.util.ArrayList;
import java.util.List;

public record ModChatLine(List<ModChatSpan> spans) {

    public static ModChatLine of(ModChatSpan... spans) {
        return new ModChatLine(List.of(spans));
    }

    public static ModChatLine text(String text, ModChatColor color) {
        return of(ModChatSpan.of(text, color));
    }

    public static ModChatLine blank() {
        return text(" ", ModChatColor.DARK_GRAY);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<ModChatSpan> spans = new ArrayList<>();

        public Builder add(String text, ModChatColor color) {
            spans.add(ModChatSpan.of(text, color));
            return this;
        }

        public Builder click(String text, ModChatColor color, String command, String hoverText) {
            spans.add(ModChatSpan.click(text, color, command, hoverText));
            return this;
        }

        public Builder copy(String text, ModChatColor color, String value, String hoverText) {
            spans.add(ModChatSpan.copy(text, color, value, hoverText));
            return this;
        }

        public Builder add(ModChatSpan span) {
            spans.add(span);
            return this;
        }

        public ModChatLine build() {
            return new ModChatLine(List.copyOf(spans));
        }
    }
}
