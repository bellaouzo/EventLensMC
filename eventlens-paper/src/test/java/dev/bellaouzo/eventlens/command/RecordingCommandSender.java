package dev.bellaouzo.eventlens.command;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.Nullable;

public final class RecordingCommandSender {

    private final List<String> messages = new ArrayList<>();
    private final boolean permitted;
    private final CommandSender sender;

    public RecordingCommandSender(boolean permitted) {
        this.permitted = permitted;
        this.sender = (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(),
                new Class<?>[] {CommandSender.class},
                (proxy, method, args) -> invoke(method.getName(), method.getReturnType(), args));
    }

    public CommandSender sender() {
        return sender;
    }

    public String joined() {
        return String.join("\n", messages);
    }

    private @Nullable Object invoke(String name, Class<?> returnType, Object[] args) {
        if ("hasPermission".equals(name)) {
            return permitted;
        }
        if ("sendMessage".equals(name)) {
            capture(args);
            return null;
        }
        if ("getName".equals(name) || "getNameLegacy".equals(name)) {
            return "Admin";
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class || returnType == long.class) {
            return 0;
        }
        if (returnType == void.class) {
            return null;
        }
        return null;
    }

    private void capture(Object[] args) {
        if (args == null || args.length == 0) {
            return;
        }
        Object last = args[args.length - 1];
        if (last instanceof Component component) {
            messages.add(plain(component));
        } else if (last instanceof String text) {
            messages.add(text);
        }
    }

    private static String plain(Component component) {
        StringBuilder text = new StringBuilder();
        appendPlain(component, text);
        return text.toString();
    }

    private static void appendPlain(Component component, StringBuilder text) {
        if (component instanceof TextComponent textComponent) {
            text.append(textComponent.content());
        }
        for (Component child : component.children()) {
            appendPlain(child, text);
        }
    }
}
