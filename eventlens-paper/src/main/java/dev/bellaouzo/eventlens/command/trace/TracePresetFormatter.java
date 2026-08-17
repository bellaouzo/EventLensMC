package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.TracePreset;
import dev.bellaouzo.eventlens.command.CommandUi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TracePresetFormatter {

    private TracePresetFormatter() {}

    static void render(CommandSender sender, EventLensCommandConfig commandConfig) {
        Map<String, TracePreset> presets = commandConfig.presets();
        if (presets.isEmpty()) {
            sender.sendMessage(Component.text("No trace presets configured.", NamedTextColor.GRAY));
            sender.sendMessage(
                    Component.text("Add entries under trace.presets in config.yml.", NamedTextColor.DARK_GRAY));
            return;
        }

        sender.sendMessage(Component.text("Trace presets:", NamedTextColor.GOLD));
        for (TracePreset preset : presets.values()) {
            String exampleEvent = "PlayerInteractEvent";
            String command = "/eventlens trace start " + exampleEvent + " --preset " + preset.name();
            sender.sendMessage(CommandUi.runCommand(preset.name(), command, presetHover(preset, command)));
        }
    }

    private static Component presetHover(TracePreset preset, String command) {
        List<String> lines = new ArrayList<>();
        lines.add("Preset: " + preset.name());
        preset.pluginName().ifPresent(value -> lines.add("plugin=" + value));
        preset.playerName().ifPresent(value -> lines.add("player=" + value));
        preset.worldName().ifPresent(value -> lines.add("world=" + value));
        preset.maxDurationMillis().ifPresent(value -> lines.add("max-duration=" + value + "ms"));
        preset.maxEventCount().ifPresent(value -> lines.add("max-events=" + value));
        preset.slowThresholdNanos().ifPresent(value -> lines.add("slow-threshold=" + value + "ns"));
        if (preset.captureStacks()) {
            lines.add("capture-stacks=true");
        }
        lines.add("");
        lines.add("Example: " + command);
        return CommandUi.hoverBlock(lines.toArray(String[]::new));
    }
}
