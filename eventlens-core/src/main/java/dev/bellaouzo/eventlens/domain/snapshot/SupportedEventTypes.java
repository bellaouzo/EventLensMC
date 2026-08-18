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
            "org.bukkit.event.entity.FoodLevelChangeEvent",
            "org.bukkit.event.player.PlayerAnimationEvent",
            "org.bukkit.event.player.PlayerBedEnterEvent",
            "org.bukkit.event.player.PlayerBedLeaveEvent",
            "org.bukkit.event.player.PlayerToggleSneakEvent",
            "org.bukkit.event.player.PlayerToggleSprintEvent",
            "org.bukkit.event.player.PlayerToggleFlightEvent",
            "org.bukkit.event.player.PlayerGameModeChangeEvent",
            "org.bukkit.event.player.PlayerItemHeldEvent",
            "org.bukkit.event.player.PlayerItemDamageEvent",
            "org.bukkit.event.player.PlayerItemBreakEvent",
            "org.bukkit.event.player.PlayerItemMendEvent",
            "org.bukkit.event.player.PlayerExpChangeEvent",
            "org.bukkit.event.player.PlayerLevelChangeEvent",
            "org.bukkit.event.player.PlayerAdvancementDoneEvent",
            "org.bukkit.event.player.PlayerHarvestBlockEvent",
            "org.bukkit.event.player.PlayerShearEntityEvent",
            "org.bukkit.event.player.PlayerRiptideEvent",
            "org.bukkit.event.player.PlayerEggThrowEvent",
            "org.bukkit.event.player.PlayerRecipeDiscoverEvent",
            "org.bukkit.event.player.PlayerArmorStandManipulateEvent",
            "org.bukkit.event.entity.PlayerLeashEntityEvent",
            "org.bukkit.event.player.PlayerUnleashEntityEvent",
            "org.bukkit.event.player.PlayerAttemptPickupItemEvent",
            "org.bukkit.event.player.PlayerPickupArrowEvent",
            "org.bukkit.event.player.PlayerLoginEvent",
            "org.bukkit.event.player.PlayerResourcePackStatusEvent",
            "org.bukkit.event.player.PlayerEditBookEvent",
            "org.bukkit.event.player.PlayerTakeLecternBookEvent",
            "org.bukkit.event.block.BlockDamageEvent",
            "org.bukkit.event.block.BlockFadeEvent",
            "org.bukkit.event.block.BlockFormEvent",
            "org.bukkit.event.block.EntityBlockFormEvent",
            "org.bukkit.event.block.BlockGrowEvent",
            "org.bukkit.event.block.BlockSpreadEvent",
            "org.bukkit.event.block.BlockFromToEvent",
            "org.bukkit.event.block.BlockPistonExtendEvent",
            "org.bukkit.event.block.BlockPistonRetractEvent",
            "org.bukkit.event.block.BlockRedstoneEvent",
            "org.bukkit.event.block.BlockDispenseEvent",
            "org.bukkit.event.block.BlockDropItemEvent",
            "org.bukkit.event.block.BlockFertilizeEvent",
            "org.bukkit.event.block.TNTPrimeEvent",
            "org.bukkit.event.block.LeavesDecayEvent",
            "org.bukkit.event.block.NotePlayEvent",
            "org.bukkit.event.block.CauldronLevelChangeEvent",
            "org.bukkit.event.block.MoistureChangeEvent",
            "org.bukkit.event.block.SpongeAbsorbEvent",
            "org.bukkit.event.entity.EntityBreedEvent",
            "org.bukkit.event.entity.EntityTameEvent",
            "org.bukkit.event.entity.EntityRegainHealthEvent",
            "org.bukkit.event.entity.EntityPotionEffectEvent",
            "org.bukkit.event.entity.EntityResurrectEvent",
            "org.bukkit.event.entity.EntityShootBowEvent",
            "org.bukkit.event.entity.EntityToggleGlideEvent",
            "org.bukkit.event.entity.EntityToggleSwimEvent",
            "org.bukkit.event.entity.EntityMountEvent",
            "org.bukkit.event.entity.EntityDismountEvent",
            "org.bukkit.event.entity.EntityTransformEvent",
            "org.bukkit.event.entity.EntityTeleportEvent",
            "org.bukkit.event.entity.EntityCombustEvent",
            "org.bukkit.event.entity.EntityDamageByBlockEvent",
            "org.bukkit.event.entity.EntityPlaceEvent",
            "org.bukkit.event.entity.ItemDespawnEvent",
            "org.bukkit.event.entity.ItemSpawnEvent",
            "org.bukkit.event.entity.PotionSplashEvent",
            "org.bukkit.event.entity.LingeringPotionSplashEvent",
            "org.bukkit.event.entity.AreaEffectCloudApplyEvent",
            "org.bukkit.event.entity.SlimeSplitEvent",
            "org.bukkit.event.entity.HorseJumpEvent",
            "org.bukkit.event.entity.PiglinBarterEvent",
            "org.bukkit.event.hanging.HangingBreakEvent",
            "org.bukkit.event.hanging.HangingBreakByEntityEvent",
            "org.bukkit.event.hanging.HangingPlaceEvent",
            "org.bukkit.event.weather.LightningStrikeEvent",
            "org.bukkit.event.weather.WeatherChangeEvent",
            "org.bukkit.event.weather.ThunderChangeEvent",
            "org.bukkit.event.world.PortalCreateEvent",
            "org.bukkit.event.world.StructureGrowEvent",
            "org.bukkit.event.raid.RaidTriggerEvent",
            "org.bukkit.event.raid.RaidFinishEvent",
            "org.bukkit.event.raid.RaidSpawnWaveEvent",
            "org.bukkit.event.world.LootGenerateEvent",
            "org.bukkit.event.world.TimeSkipEvent",
            "org.bukkit.event.world.ChunkLoadEvent",
            "org.bukkit.event.world.ChunkUnloadEvent",
            "org.bukkit.event.inventory.PrepareAnvilEvent",
            "org.bukkit.event.inventory.PrepareItemCraftEvent",
            "org.bukkit.event.inventory.PrepareSmithingEvent",
            "org.bukkit.event.inventory.PrepareGrindstoneEvent",
            "org.bukkit.event.enchantment.EnchantItemEvent",
            "org.bukkit.event.inventory.SmithItemEvent",
            "org.bukkit.event.inventory.TradeSelectEvent",
            "org.bukkit.event.inventory.BrewEvent",
            "org.bukkit.event.inventory.FurnaceSmeltEvent",
            "org.bukkit.event.inventory.FurnaceBurnEvent");
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
