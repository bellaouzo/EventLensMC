package dev.bellaouzo.eventlens.command.trace;

import java.nio.file.Path;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class ExportPathMessages {

    private ExportPathMessages() {}

    static void sendSavedPath(CommandSender sender, Path path) {
        Path absolute = path.toAbsolutePath().normalize();
        Path directory = absolute.getParent();
        String filePath = absolute.toString();

        sender.sendMessage(pathLine("Saved to", filePath, "Click to copy file path"));
        if (directory != null) {
            sender.sendMessage(pathLine("Folder", directory.toString(), "Click to copy folder path"));
        }
    }

    private static Component pathLine(String label, String path, String hoverText) {
        return Component.text(label + ": ", NamedTextColor.GRAY)
                .append(Component.text(path, NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.copyToClipboard(path))
                        .hoverEvent(HoverEvent.showText(Component.text(hoverText, NamedTextColor.WHITE))));
    }
}
