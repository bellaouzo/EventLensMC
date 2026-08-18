package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.chat.ModChatClick;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatSpan;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

final class ForgeChatRenderer {

    private ForgeChatRenderer() {}

    static Component render(ModChatLine line) {
        MutableComponent component = Component.empty();
        for (ModChatSpan span : line.spans()) {
            MutableComponent piece = Component.literal(span.text()).withStyle(color(span.color()));
            Style style = piece.getStyle();
            if (span.bold()) {
                style = style.withBold(true);
            }
            if (span.underline()) {
                style = style.withUnderlined(true);
            }
            if (span.clickKind() == ModChatClick.RUN_COMMAND && !span.clickValue().isBlank()) {
                style = style.withClickEvent(
                        new ClickEvent.RunCommand(clientClickCommand(span.clickValue())));
            } else if (span.clickKind() == ModChatClick.COPY && !span.clickValue().isBlank()) {
                style = style.withClickEvent(new ClickEvent.CopyToClipboard(span.clickValue()));
            }
            if (!span.hoverText().isBlank()) {
                style = style.withHoverEvent(
                        new HoverEvent.ShowText(Component.literal(span.hoverText())));
            }
            component.append(piece.withStyle(style));
        }
        return component;
    }

    private static String clientClickCommand(String command) {
        if ("/eventlens ui".equals(command) || "/el ui".equals(command)) {
            return "/eventlensui";
        }
        return command;
    }

    private static ChatFormatting color(ModChatColor color) {
        return switch (color) {
            case GOLD -> ChatFormatting.GOLD;
            case YELLOW -> ChatFormatting.YELLOW;
            case GRAY -> ChatFormatting.GRAY;
            case DARK_GRAY -> ChatFormatting.DARK_GRAY;
            case GREEN -> ChatFormatting.GREEN;
            case RED -> ChatFormatting.RED;
            case AQUA -> ChatFormatting.AQUA;
            case WHITE -> ChatFormatting.WHITE;
        };
    }
}
