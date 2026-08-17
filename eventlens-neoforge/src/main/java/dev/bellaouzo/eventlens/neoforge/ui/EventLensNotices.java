package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatClick;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.command.ModCommandNotices;
import dev.bellaouzo.eventlens.modcommon.command.ModTraceFormatter;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class EventLensNotices {

    private EventLensNotices() {}

    public static void export(ModTraceResults.ExportResult result) {
        EventLensToasts.show(result.message());
        for (ModChatLine line : ModTraceFormatter.export(result)) {
            chat(copyable(line));
        }
    }

    public static void action(String message) {
        EventLensToasts.show(message);
    }

    public static void command(List<String> args, List<ModChatLine> lines) {
        ModCommandNotices.toastMessage(args, lines).ifPresent(EventLensToasts::show);
    }

    private static void chat(Component component) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(component, false);
        }
    }

    private static Component copyable(ModChatLine line) {
        MutableComponent component = Component.empty();
        line.spans().forEach(span -> {
            MutableComponent piece = Component.literal(span.text());
            Style style = piece.getStyle().withColor(color(span.color()));
            if (span.clickKind() == ModChatClick.COPY && !span.clickValue().isBlank()) {
                style = style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, span.clickValue()))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(span.hoverText())));
            }
            component.append(piece.withStyle(style));
        });
        return component;
    }

    private static ChatFormatting color(dev.bellaouzo.eventlens.modcommon.chat.ModChatColor color) {
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
