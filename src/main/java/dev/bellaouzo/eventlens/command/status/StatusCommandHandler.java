package dev.bellaouzo.eventlens.command.status;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.StatusQueryService;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.domain.status.EventLensStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class StatusCommandHandler {

    public static final String PERMISSION = "eventlens.command.status";

    private final StatusQueryService statusQueryService;

    public StatusCommandHandler(StatusQueryService statusQueryService, EventLensCommandConfig commandConfig) {
        this.statusQueryService = statusQueryService;
        this.commandConfig = commandConfig;
    }

    private final EventLensCommandConfig commandConfig;

    public void handle(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }

        EventLensStatus status = statusQueryService.queryStatus(Bukkit.getVersion(), Bukkit.getBukkitVersion());
        StatusCommandFormatter.render(sender, status, commandConfig);
    }
}
