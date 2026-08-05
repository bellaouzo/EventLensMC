package dev.bellaouzo.eventlens.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class CommandUi {

    private CommandUi() {}

    public static @NonNull Component runCommand(
            @NonNull String label, @NonNull String command, @NonNull Component hover) {
        return Component.text(label, NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(hover));
    }

    public static @NonNull Component runCommand(
            @NonNull String label, @NonNull String command, @NonNull String hoverText) {
        return runCommand(label, command, Component.text(hoverText, NamedTextColor.GRAY));
    }

    public static @NonNull Component copyPath(@NonNull String label, @NonNull String path, @NonNull String hoverText) {
        return Component.text(label, NamedTextColor.GRAY)
                .append(Component.text(path, NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.copyToClipboard(path))
                        .hoverEvent(HoverEvent.showText(Component.text(hoverText, NamedTextColor.WHITE))));
    }

    public static @NonNull Component textWithHover(@NonNull String text, @NonNull Component hover) {
        return Component.text(text, NamedTextColor.YELLOW).hoverEvent(HoverEvent.showText(hover));
    }

    public static @NonNull Component labeledLine(@NonNull String label, @NonNull Component value) {
        return Component.text(label + ": ", NamedTextColor.GRAY).append(value);
    }

    public static @NonNull Component labeledLine(
            @NonNull String label, @NonNull String value, @NonNull NamedTextColor color) {
        return labeledLine(label, Component.text(value, color));
    }

    public static @NonNull Component enabledState(boolean enabled) {
        return Component.text(enabled ? "enabled" : "disabled", enabled ? NamedTextColor.GREEN : NamedTextColor.RED);
    }

    public static @NonNull Component capabilityChip(
            @NonNull String label, boolean enabled, @NonNull String enabledHint, @NonNull String disabledHint) {
        NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY;
        String prefix = enabled ? "\u2713 " : "\u2717 ";
        return Component.text(prefix + label, color)
                .hoverEvent(HoverEvent.showText(
                        Component.text(enabled ? enabledHint : disabledHint, NamedTextColor.WHITE)));
    }

    public static @NonNull Component codeBadge(@NonNull String code, @NonNull NamedTextColor color) {
        return Component.text("[" + code + "]", color);
    }

    public static @NonNull Component sectionTitle(@NonNull String title) {
        return Component.text(title, NamedTextColor.GOLD).decorate(TextDecoration.BOLD);
    }

    public static @NonNull Component divider() {
        return Component.text(
                "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500",
                NamedTextColor.DARK_GRAY);
    }

    public static @NonNull Component actionBar(@NonNull Component... actions) {
        Component bar = Component.text("Actions: ", NamedTextColor.GRAY);
        boolean first = true;
        for (Component action : actions) {
            if (action == null) {
                continue;
            }
            if (!first) {
                bar = bar.append(Component.text("  ", NamedTextColor.DARK_GRAY));
            }
            bar = bar.append(action);
            first = false;
        }
        return bar;
    }

    public static @NonNull Component hoverBlock(@NonNull String... lines) {
        Component block = Component.empty();
        boolean first = true;
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            if (!first) {
                block = block.append(Component.newline());
            }
            block = block.append(Component.text(line, NamedTextColor.WHITE));
            first = false;
        }
        return block;
    }

    public static @Nullable String slashCommand(@NonNull String command) {
        if (command.isBlank()) {
            return null;
        }
        return command.startsWith("/") ? command : "/" + command;
    }
}
