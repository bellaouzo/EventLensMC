package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.report.TraceRegressionReport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TracePluginCompareFormatter {

    private TracePluginCompareFormatter() {}

    static void render(CommandSender sender, TraceRegressionReport report) {
        sender.sendMessage(Component.text("Trace comparison:", NamedTextColor.GOLD));
        sender.sendMessage(CommandUi.labeledLine("Scope", Component.text(report.scopeLabel(), NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text(report.summaryText(), NamedTextColor.GRAY));
    }
}
