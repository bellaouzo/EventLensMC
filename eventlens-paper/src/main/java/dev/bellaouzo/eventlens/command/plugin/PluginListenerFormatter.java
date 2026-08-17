package dev.bellaouzo.eventlens.command.plugin;

import dev.bellaouzo.eventlens.command.ChatPagination;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerBinding;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerPage;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class PluginListenerFormatter {

    private PluginListenerFormatter() {}

    static void renderPage(CommandSender sender, PluginListenerPage page, OutputDetailLevel detailLevel) {
        sender.sendMessage(Component.text(
                "Listeners for " + page.pluginName()
                        + (page.filteredEventClassName() != null
                                ? " on " + CommandText.simpleName(page.filteredEventClassName())
                                : "")
                        + " (page "
                        + page.page()
                        + "/"
                        + page.totalPages()
                        + ", "
                        + page.totalListeners()
                        + " total)",
                NamedTextColor.GOLD));

        for (PluginListenerBinding binding : page.bindings()) {
            renderBinding(sender, binding, detailLevel);
        }

        ChatPagination.sendNavigation(
                sender,
                page.page(),
                page.totalPages(),
                buildPageCommand(page, page.page() - 1),
                buildPageCommand(page, page.page() + 1));
    }

    static void renderBinding(CommandSender sender, PluginListenerBinding binding, OutputDetailLevel detailLevel) {
        String simpleEvent = CommandText.simpleName(binding.eventClassName());
        sender.sendMessage(Component.text(
                "[" + simpleEvent + "] #" + binding.registration().registrationOrder() + " "
                        + binding.registration().priority(),
                NamedTextColor.GRAY));

        Component methodLine = Component.text(
                        "  " + CommandText.simpleName(binding.registration().listenerClassName()) + "#"
                                + binding.registration().methodName(),
                        NamedTextColor.WHITE)
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                        "Event: " + binding.eventClassName(),
                        "Class: " + binding.registration().listenerClassName(),
                        "Method: " + binding.registration().methodName(),
                        "Priority: " + binding.registration().priority(),
                        "Ignore cancelled: " + binding.registration().ignoreCancelled())));

        if (detailLevel == OutputDetailLevel.VERBOSE) {
            methodLine = methodLine.append(Component.text(
                    " (ignoreCancelled=" + binding.registration().ignoreCancelled() + ")", NamedTextColor.DARK_GRAY));
        }

        sender.sendMessage(methodLine);
    }

    private static String buildPageCommand(PluginListenerPage page, int targetPage) {
        StringBuilder command = new StringBuilder("/eventlens plugin ")
                .append(page.pluginName())
                .append(" listeners");
        if (page.filteredEventClassName() != null) {
            command.append(' ').append(CommandText.simpleName(page.filteredEventClassName()));
        }
        command.append(' ').append(targetPage);
        return command.toString();
    }
}
