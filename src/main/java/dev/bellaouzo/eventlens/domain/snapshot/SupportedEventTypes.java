package dev.bellaouzo.eventlens.domain.snapshot;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class SupportedEventTypes {

    private static final List<String> BASE_CLASS_NAMES = List.of(
            "org.bukkit.event.block.BlockBreakEvent",
            "org.bukkit.event.player.PlayerInteractEvent",
            "org.bukkit.event.inventory.InventoryClickEvent",
            "org.bukkit.event.entity.EntityDamageEvent",
            "org.bukkit.event.player.PlayerTeleportEvent",
            "org.bukkit.event.player.PlayerMoveEvent",
            "org.bukkit.event.block.BlockPlaceEvent",
            "org.bukkit.event.entity.EntityDeathEvent",
            "org.bukkit.event.player.PlayerJoinEvent",
            "org.bukkit.event.player.PlayerQuitEvent",
            "org.bukkit.event.entity.EntitySpawnEvent",
            "org.bukkit.event.player.PlayerCommandPreprocessEvent",
            "io.papermc.paper.event.player.AsyncChatEvent");
    private static final Set<String> additionalClassNames = new CopyOnWriteArraySet<>();

    private SupportedEventTypes() {}

    public static List<String> classNames() {
        return mergedClassNames();
    }

    public static List<String> simpleNames() {
        return mergedClassNames().stream()
                .map(SupportedEventTypes::toSimpleName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public static boolean isSupported(String className) {
        if (className == null) {
            return false;
        }
        return mergedClassNames().contains(className);
    }

    private static String toSimpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0 || lastDot == className.length() - 1) {
            return className;
        }
        return className.substring(lastDot + 1);
    }

    public static String displaySimpleName(String className) {
        return toSimpleName(className);
    }

    public static String formatSimpleNameList() {
        return String.join(", ", simpleNames());
    }

    public static boolean isSupportedSimpleName(String simpleName) {
        if (simpleName == null || simpleName.isBlank()) {
            return false;
        }
        return simpleNames().stream().anyMatch(name -> name.equalsIgnoreCase(simpleName.trim()));
    }

    public static void setAdditionalEventClassNames(List<String> classNames) {
        additionalClassNames.clear();
        if (classNames == null) {
            return;
        }
        classNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .forEach(additionalClassNames::add);
    }

    private static List<String> mergedClassNames() {
        return java.util.stream.Stream.concat(BASE_CLASS_NAMES.stream(), additionalClassNames.stream())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }
}
