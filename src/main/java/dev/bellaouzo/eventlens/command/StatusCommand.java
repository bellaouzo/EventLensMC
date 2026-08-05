package dev.bellaouzo.eventlens.command;

import dev.bellaouzo.eventlens.EventLens;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class StatusCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "eventlens.command.status";

    private final EventLens plugin;
    private final TraceSessionManager traceSessionManager;

    public StatusCommand(EventLens plugin, TraceSessionManager traceSessionManager) {
        this.plugin = plugin;
        this.traceSessionManager = traceSessionManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(Component.text("Usage: /eventlens status", NamedTextColor.YELLOW));
            return true;
        }

        sender.sendMessage(Component.text("EventLens status", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: " + plugin.getDescription().getVersion(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Target platform: Paper 26.2", NamedTextColor.GRAY));
        sender.sendMessage(Component.text(
                "Tracing enabled: " + traceSessionManager.isTracingEnabled(),
                NamedTextColor.GRAY
        ));
        sender.sendMessage(Component.text(
                "Active trace sessions: " + traceSessionManager.getActiveSessionCount(),
                NamedTextColor.GRAY
        ));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return List.of("status");
        }

        return Collections.emptyList();
    }
}
