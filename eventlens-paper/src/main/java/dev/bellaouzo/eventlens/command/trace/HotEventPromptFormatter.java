package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.observability.SamplingPolicy;
import dev.bellaouzo.eventlens.domain.trace.TraceStartResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class HotEventPromptFormatter {

    private HotEventPromptFormatter() {}

    static void render(CommandSender sender, TraceStartResult.Failure failure) {
        sender.sendMessage(Component.text(failure.message(), NamedTextColor.RED));
        sender.sendMessage(Component.text(
                "Hot events are sampled heavily and still add overhead. Use a narrow filter and keep sessions short.",
                NamedTextColor.YELLOW));
        failure.confirmCommand()
                .ifPresent(command -> sender.sendMessage(CommandUi.labeledLine(
                        "Confirm",
                        CommandUi.runCommand(
                                "[Start trace anyway]",
                                command,
                                CommandUi.hoverBlock(
                                        "Acknowledge performance impact",
                                        "and start this hot-event trace.",
                                        "",
                                        command)))));
        sender.sendMessage(Component.text(
                "Sample rate: 1 in " + new SamplingPolicy().hotEventSampleRate() + " matching dispatches.",
                NamedTextColor.GRAY));
    }
}
