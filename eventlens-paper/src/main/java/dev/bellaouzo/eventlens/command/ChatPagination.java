package dev.bellaouzo.eventlens.command;

import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ChatPagination {

    private ChatPagination() {}

    public static void sendNavigation(
            CommandSender sender,
            int currentPage,
            int totalPages,
            @Nullable String previousCommand,
            @Nullable String nextCommand) {
        buildNavigation(currentPage, totalPages, previousCommand, nextCommand).ifPresent(sender::sendMessage);
    }

    @SuppressWarnings("null")
    public static Optional<Component> buildNavigation(
            int currentPage, int totalPages, @Nullable String previousCommand, @Nullable String nextCommand) {
        if (totalPages <= 1) {
            return Optional.empty();
        }

        Component navigation = Component.text("Page " + currentPage + "/" + totalPages, NamedTextColor.GRAY);

        if (currentPage > 1 && previousCommand != null && !previousCommand.isBlank()) {
            @NonNull
            Component previousButton = pageButton("◀ Previous", previousCommand, "Go to page " + (currentPage - 1));
            navigation = Objects.requireNonNull(navigation
                    .append(Component.text(" · ", NamedTextColor.DARK_GRAY))
                    .append(previousButton));
        }

        if (currentPage < totalPages && nextCommand != null && !nextCommand.isBlank()) {
            @NonNull Component nextButton = pageButton("Next ▶", nextCommand, "Go to page " + (currentPage + 1));
            navigation = Objects.requireNonNull(navigation
                    .append(Component.text(" · ", NamedTextColor.DARK_GRAY))
                    .append(nextButton));
        }

        return Optional.of(navigation);
    }

    private static @NonNull Component pageButton(
            @NonNull String label, @NonNull String command, @NonNull String hoverText) {
        return Objects.requireNonNull(Component.text("[" + label + "]", NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(hoverText, NamedTextColor.GRAY))));
    }
}
