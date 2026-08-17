package dev.bellaouzo.eventlens.command.plugin;

import dev.bellaouzo.eventlens.command.CommandLiterals;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.plugin.PluginCoInteraction;
import dev.bellaouzo.eventlens.domain.plugin.PluginDescriptor;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerBinding;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerInventory;
import dev.bellaouzo.eventlens.domain.plugin.PluginProfile;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class PluginProfileFormatter {

    private static final int MAX_BRIEF_EVENTS = 8;
    private static final int MAX_NORMAL_EVENTS = 16;
    private static final int MAX_CO_PLUGINS = 8;

    private PluginProfileFormatter() {}

    static void renderProfile(CommandSender sender, PluginProfile profile, OutputDetailLevel detailLevel) {
        PluginDescriptor descriptor = profile.descriptor();
        PluginListenerInventory inventory = profile.inventory();

        sender.sendMessage(Component.text("Plugin profile: " + descriptor.name(), NamedTextColor.GOLD));
        renderLoadState(sender, descriptor);
        renderDependencies(sender, descriptor, detailLevel);
        renderEventSummary(sender, inventory, detailLevel);
        renderCountMap(sender, "Listeners by event", inventory.listenerCountByEvent(), detailLevel);
        renderCountMap(sender, "Listeners by priority", inventory.listenerCountByPriority(), detailLevel);
        renderCoInteractions(sender, profile.coInteractions(), detailLevel);
        PluginProfileTraceFormatter.renderTraceStatistics(sender, profile.traceStatistics(), detailLevel);
        PluginProfileTraceFormatter.renderRecentChanges(sender, profile.traceStatistics(), detailLevel);

        if (detailLevel == OutputDetailLevel.VERBOSE && !inventory.bindings().isEmpty()) {
            sender.sendMessage(Component.text("Registered listeners (summary):", NamedTextColor.YELLOW));
            for (PluginListenerBinding binding : inventory.bindings()) {
                PluginListenerFormatter.renderBinding(sender, binding, OutputDetailLevel.NORMAL);
            }
        }

        sender.sendMessage(CommandUi.labeledLine(
                "Details",
                CommandUi.runCommand(
                        "[listeners]",
                        "/eventlens plugin " + descriptor.name() + " listeners",
                        "Paginated listener list for " + descriptor.name())));
        sender.sendMessage(CommandUi.actionBar(
                CommandUi.runCommand(
                        "[Start trace]",
                        CommandLiterals.TRACE_START_PREFIX + "PlayerInteractEvent --preset plugin-focus --plugin "
                                + descriptor.name(),
                        "Start a plugin-focused trace preset for " + descriptor.name()),
                CommandUi.runCommand(
                        "[Compare baseline]",
                        "/eventlens trace baseline list",
                        "List baselines, then compare with --plugin " + descriptor.name())));
    }

    private static void renderLoadState(CommandSender sender, PluginDescriptor descriptor) {
        NamedTextColor stateColor = descriptor.enabled() ? NamedTextColor.GREEN : NamedTextColor.RED;
        sender.sendMessage(CommandUi.labeledLine(
                "State", Component.text(descriptor.enabled() ? "enabled" : "disabled", stateColor)));
        sender.sendMessage(
                CommandUi.labeledLine("Version", Component.text(descriptor.version(), NamedTextColor.WHITE)));
    }

    private static void renderDependencies(
            CommandSender sender, PluginDescriptor descriptor, OutputDetailLevel detailLevel) {
        if (detailLevel == OutputDetailLevel.BRIEF) {
            sender.sendMessage(CommandUi.labeledLine(
                    "Dependencies",
                    Component.text(
                            descriptor.hardDependencies().size()
                                    + " hard, "
                                    + descriptor.softDependencies().size()
                                    + " soft",
                            NamedTextColor.WHITE)));
            return;
        }

        sender.sendMessage(CommandUi.labeledLine(
                "Hard dependencies", formatNameList(descriptor.hardDependencies(), NamedTextColor.WHITE)));
        sender.sendMessage(CommandUi.labeledLine(
                "Soft dependencies", formatNameList(descriptor.softDependencies(), NamedTextColor.WHITE)));

        if (detailLevel == OutputDetailLevel.VERBOSE) {
            sender.sendMessage(
                    CommandUi.labeledLine("Load before", formatNameList(descriptor.loadBefore(), NamedTextColor.GRAY)));
            sender.sendMessage(
                    CommandUi.labeledLine("Provides", formatNameList(descriptor.provides(), NamedTextColor.GRAY)));
        }
    }

    private static void renderEventSummary(
            CommandSender sender, PluginListenerInventory inventory, OutputDetailLevel detailLevel) {
        int eventCount = inventory.eventClassNames().size();
        int listenerCount = inventory.bindings().size();
        sender.sendMessage(CommandUi.labeledLine(
                "Events listened",
                Component.text(eventCount + " events, " + listenerCount + " listeners", NamedTextColor.WHITE)));

        int limit = eventLimitForDetailLevel(detailLevel);

        List<String> events = inventory.eventClassNames();
        for (int index = 0; index < Math.min(events.size(), limit); index++) {
            String eventClassName = events.get(index);
            String simpleName = CommandText.simpleName(eventClassName);
            sender.sendMessage(CommandUi.runCommand(
                    "  " + simpleName,
                    "/eventlens plugin " + inventory.pluginName() + " listeners " + simpleName,
                    "Listeners on " + simpleName));
        }

        if (events.size() > limit) {
            sender.sendMessage(Component.text(
                    "  ... and " + (events.size() - limit) + " more (use listeners subcommand).", NamedTextColor.GRAY));
        }
    }

    private static void renderCountMap(
            CommandSender sender, String title, Map<String, Integer> counts, OutputDetailLevel detailLevel) {
        if (counts.isEmpty()) {
            if (detailLevel != OutputDetailLevel.BRIEF) {
                sender.sendMessage(CommandUi.labeledLine(title, Component.text("none", NamedTextColor.GRAY)));
            }
            return;
        }

        sender.sendMessage(Component.text(title + ":", NamedTextColor.YELLOW));
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)))
                .limit(detailLevel == OutputDetailLevel.BRIEF ? 5 : Integer.MAX_VALUE)
                .forEach(entry -> sender.sendMessage(
                        Component.text("  " + entry.getKey() + ": " + entry.getValue(), NamedTextColor.WHITE)
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text(
                                        entry.getKey() + " = " + entry.getValue(), NamedTextColor.GRAY)))));
    }

    private static void renderCoInteractions(
            CommandSender sender, List<PluginCoInteraction> interactions, OutputDetailLevel detailLevel) {
        if (interactions.isEmpty()) {
            if (detailLevel != OutputDetailLevel.BRIEF) {
                sender.sendMessage(CommandUi.labeledLine(
                        "Common interactions", Component.text("none detected", NamedTextColor.GRAY)));
            }
            return;
        }

        sender.sendMessage(Component.text("Common interactions:", NamedTextColor.YELLOW));
        int limit = detailLevel == OutputDetailLevel.BRIEF ? 5 : MAX_CO_PLUGINS;
        for (int index = 0; index < Math.min(interactions.size(), limit); index++) {
            PluginCoInteraction interaction = interactions.get(index);
            sender.sendMessage(Component.text(
                            "  " + interaction.pluginName() + ": shared events "
                                    + interaction.sharedEventCount() + ", trace co-dispatches "
                                    + interaction.traceCoDispatchCount(),
                            NamedTextColor.WHITE)
                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                            "Plugin: " + interaction.pluginName(),
                            "Shared events: " + interaction.sharedEventCount(),
                            "Trace co-dispatches: " + interaction.traceCoDispatchCount()))));
        }

        if (interactions.size() > limit) {
            sender.sendMessage(
                    Component.text("  ... and " + (interactions.size() - limit) + " more.", NamedTextColor.GRAY));
        }
    }

    private static int eventLimitForDetailLevel(OutputDetailLevel detailLevel) {
        if (detailLevel == OutputDetailLevel.BRIEF) {
            return MAX_BRIEF_EVENTS;
        }
        if (detailLevel == OutputDetailLevel.NORMAL) {
            return MAX_NORMAL_EVENTS;
        }
        return Integer.MAX_VALUE;
    }

    private static Component formatNameList(List<String> names, NamedTextColor color) {
        if (names.isEmpty()) {
            return Component.text("none", NamedTextColor.GRAY);
        }
        return Component.text(String.join(", ", names), color);
    }
}
