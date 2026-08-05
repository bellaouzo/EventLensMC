package dev.bellaouzo.eventlens.testkit;

import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class TestKitCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("eventlenstestkit.command")) {
            sender.sendMessage("You do not have permission.");
            return true;
        }
        if (args.length >= 2 && "mode".equalsIgnoreCase(args[0])) {
            TestKitMode mode = parseMode(args[1]);
            if (mode == null) {
                sender.sendMessage("Usage: /eltest mode <passive|trace|exception|slow>");
                return true;
            }
            TestKitState.setMode(mode);
            sender.sendMessage("EventLensTestTarget mode set to " + mode.name().toLowerCase(Locale.ROOT));
            if (mode == TestKitMode.PASSIVE) {
                sender.sendMessage("Gameplay listeners are inactive. Break/place works normally.");
            } else {
                sender.sendMessage("Trace scenario active: breaks and block interactions may be cancelled.");
            }
            return true;
        }
        sender.sendMessage("Usage: /eltest mode <passive|trace|exception|slow>");
        return true;
    }

    @Override
    public @Nullable java.util.List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {
        if (args.length == 1) {
            return java.util.List.of("mode");
        }
        if (args.length == 2 && "mode".equalsIgnoreCase(args[0])) {
            return java.util.List.of("passive", "trace", "exception", "slow");
        }
        return java.util.List.of();
    }

    private static TestKitMode parseMode(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "passive", "normal", "off" -> TestKitMode.PASSIVE;
            case "trace", "on" -> TestKitMode.TRACE;
            case "exception" -> TestKitMode.EXCEPTION;
            case "slow" -> TestKitMode.SLOW;
            default -> null;
        };
    }
}
