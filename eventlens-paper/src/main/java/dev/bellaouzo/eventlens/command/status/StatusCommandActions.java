package dev.bellaouzo.eventlens.command.status;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.command.CommandLiterals;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.instrumentation.AgentInstallHints;
import dev.bellaouzo.eventlens.domain.status.EventLensStatus;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class StatusCommandActions {

    private StatusCommandActions() {}

    static Component render(CommandSender sender, EventLensStatus status, EventLensCommandConfig commandConfig) {
        List<Component> actions = new ArrayList<>();
        if (EventLensPermissions.has(sender, StatusCommandHandler.PERMISSION)) {
            actions.add(CommandUi.runCommand("[Refresh]", "/eventlens status", "Reload status output"));
        }
        if (EventLensPermissions.hasTrace(sender, "list")) {
            actions.add(CommandUi.runCommand("[Sessions]", "/eventlens trace list", "List active trace sessions"));
        }
        if (EventLensPermissions.hasTrace(sender, "presets")) {
            actions.add(
                    CommandUi.runCommand("[Presets]", "/eventlens trace presets", "Browse configured trace presets"));
        }
        if (EventLensPermissions.hasTrace(sender, "start")) {
            if (commandConfig.devMode() && commandConfig.preset("dev-debug").isPresent()) {
                actions.add(CommandUi.runCommand(
                        "[Dev trace]",
                        CommandLiterals.TRACE_START_PREFIX
                                + "PlayerInteractEvent --preset dev-debug --plugin EventLensTestTarget",
                        "Start dev-debug trace against testkit"));
            } else {
                actions.add(CommandUi.runCommand(
                        "[Start trace]",
                        CommandLiterals.TRACE_START_PREFIX + "PlayerInteractEvent",
                        "Start tracing PlayerInteractEvent"));
            }
        }
        if (EventLensPermissions.has(sender, EventLensPermissions.LISTENERS)) {
            actions.add(CommandUi.runCommand(
                    "[Listeners]",
                    "/eventlens listeners PlayerInteractEvent",
                    "Inspect listeners for PlayerInteractEvent"));
        }
        if (EventLensPermissions.hasTrace(sender, "history") && status.activeSessionCount() == 0) {
            actions.add(CommandUi.runCommand("[History]", "/eventlens trace history", "View recent trace history"));
        }
        if (!status.agentAttached()) {
            status.agentArgument()
                    .ifPresent(argument -> actions.add(Component.text("[Copy JVM arg]", NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.copyToClipboard(argument))
                            .hoverEvent(HoverEvent.showText(Component.text(
                                    "Copy Paper -javaagent argument.\nRestart the server after adding it.",
                                    NamedTextColor.GRAY)))));
            actions.add(Component.text("[Agent guide]", NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.copyToClipboard(AgentInstallHints.README_URL))
                    .hoverEvent(HoverEvent.showText(Component.text(
                            "Copy README link with Java agent install steps.", NamedTextColor.GRAY))));
        } else {
            status.agentArgument()
                    .ifPresent(argument -> actions.add(Component.text("[Copy JVM arg]", NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.copyToClipboard(argument))
                            .hoverEvent(HoverEvent.showText(Component.text(argument, NamedTextColor.GRAY)))));
        }
        return CommandUi.actionBar(actions.toArray(Component[]::new));
    }
}
