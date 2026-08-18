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
            "io.papermc.paper.event.player.AsyncChatEvent",
            "org.bukkit.event.inventory.InventoryOpenEvent",
            "org.bukkit.event.inventory.InventoryCloseEvent",
            "org.bukkit.event.inventory.InventoryDragEvent",
            "org.bukkit.event.player.PlayerDropItemEvent",
            "org.bukkit.event.entity.EntityPickupItemEvent",
            "org.bukkit.event.entity.ProjectileLaunchEvent",
            "org.bukkit.event.entity.ProjectileHitEvent",
            "org.bukkit.event.entity.CreatureSpawnEvent",
            "org.bukkit.event.server.ServerCommandEvent",
            "org.bukkit.event.entity.EntityDamageByEntityEvent",
            "org.bukkit.event.entity.EntityExplodeEvent",
            "org.bukkit.event.block.BlockExplodeEvent",
            "org.bukkit.event.entity.ExplosionPrimeEvent",
            "org.bukkit.event.player.PlayerInteractEntityEvent",
            "org.bukkit.event.player.PlayerInteractAtEntityEvent",
            "org.bukkit.event.player.PlayerItemConsumeEvent",
            "org.bukkit.event.player.PlayerRespawnEvent",
            "org.bukkit.event.player.PlayerChangedWorldEvent",
            "org.bukkit.event.player.PlayerBucketEmptyEvent",
            "org.bukkit.event.player.PlayerBucketFillEvent",
            "org.bukkit.event.block.SignChangeEvent",
            "org.bukkit.event.inventory.CraftItemEvent",
            "org.bukkit.event.entity.EntityChangeBlockEvent",
            "org.bukkit.event.block.BlockIgniteEvent",
            "org.bukkit.event.block.BlockBurnEvent",
            "org.bukkit.event.vehicle.VehicleEnterEvent",
            "org.bukkit.event.vehicle.VehicleExitEvent",
            "org.bukkit.event.entity.EntityTargetEvent",
            "org.bukkit.event.player.PlayerFishEvent",
            "org.bukkit.event.player.PlayerPortalEvent",
            "org.bukkit.event.player.PlayerKickEvent",
            "org.bukkit.event.player.PlayerSwapHandItemsEvent",
            "org.bukkit.event.entity.FoodLevelChangeEvent");
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
