package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.PlayerPreferencesService;
import dev.bellaouzo.eventlens.command.CommandLiterals;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class TracePreferenceCommandHandler {

    private final PlayerPreferencesService playerPreferencesService;

    TracePreferenceCommandHandler(PlayerPreferencesService playerPreferencesService) {
        this.playerPreferencesService = playerPreferencesService;
    }

    void handleHistory(CommandSender sender) {
        if (!EventLensPermissions.hasTrace(sender, "history")) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        UUID playerId = playerUuid(sender).orElse(null);
        if (playerId == null) {
            sender.sendMessage(
                    Component.text("Recent trace history is only available to players.", NamedTextColor.YELLOW));
            return;
        }
        TraceHistoryFormatter.renderRecent(sender, playerPreferencesService.recentTraces(playerId));
    }

    void handleFavorite(CommandSender sender, String[] args) {
        if (!EventLensPermissions.hasTrace(sender, CommandLiterals.SUBCOMMAND_FAVORITE)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        UUID playerId = playerUuid(sender).orElse(null);
        if (playerId == null) {
            sender.sendMessage(Component.text("Favorites are only available to players.", NamedTextColor.YELLOW));
            return;
        }
        if (args.length < 3) {
            TraceHistoryFormatter.renderFavorites(sender, playerPreferencesService.favorites(playerId));
            return;
        }

        switch (args[2].toLowerCase(Locale.ROOT)) {
            case "list" -> TraceHistoryFormatter.renderFavorites(sender, playerPreferencesService.favorites(playerId));
            case "add" -> addFavorite(sender, playerId, args);
            case "remove" -> removeFavorite(sender, playerId, args);
            default -> TraceHistoryFormatter.renderFavorites(sender, playerPreferencesService.favorites(playerId));
        }
    }

    void handlePresets(CommandSender sender, EventLensCommandConfig commandConfig) {
        if (!EventLensPermissions.hasTrace(sender, "presets")) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        TracePresetFormatter.render(sender, commandConfig);
    }

    private void addFavorite(CommandSender sender, UUID playerId, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /eventlens trace favorite add <event>", NamedTextColor.YELLOW));
            return;
        }
        try {
            playerPreferencesService.addFavorite(playerId, args[3]);
            sender.sendMessage(Component.text("Added favorite event: " + args[3], NamedTextColor.GREEN));
        } catch (IllegalStateException ex) {
            String message = ex.getMessage();
            sender.sendMessage(
                    Component.text(message == null ? "Could not add favorite." : message, NamedTextColor.RED));
        }
    }

    private void removeFavorite(CommandSender sender, UUID playerId, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(
                    Component.text("Usage: /eventlens trace favorite remove <event>", NamedTextColor.YELLOW));
            return;
        }
        if (playerPreferencesService.removeFavorite(playerId, args[3])) {
            sender.sendMessage(Component.text("Removed favorite event: " + args[3], NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Favorite not found: " + args[3], NamedTextColor.YELLOW));
        }
    }

    private static java.util.Optional<UUID> playerUuid(CommandSender sender) {
        if (sender instanceof Player player) {
            return java.util.Optional.of(player.getUniqueId());
        }
        return java.util.Optional.empty();
    }
}
