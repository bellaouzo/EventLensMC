package dev.bellaouzo.eventlens.modcommon;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class SupportedModEventTypes {

    public static final String CLIENT_TICK_EVENT = "dev.bellaouzo.eventlens.runtime.ClientTickEvent";
    public static final String CLIENT_WORLD_TICK_EVENT = "dev.bellaouzo.eventlens.runtime.ClientWorldTickEvent";
    public static final String CLIENT_PLAYER_TICK_EVENT = "dev.bellaouzo.eventlens.runtime.ClientPlayerTickEvent";
    public static final String CLIENT_CHAT_EVENT = "dev.bellaouzo.eventlens.runtime.ClientChatEvent";
    public static final String CLIENT_CHAT_RECEIVED_EVENT = "dev.bellaouzo.eventlens.runtime.ClientChatReceivedEvent";
    public static final String CLIENT_SCREEN_OPEN_EVENT = "dev.bellaouzo.eventlens.runtime.ClientScreenOpenEvent";
    public static final String CLIENT_SCREEN_CLOSE_EVENT = "dev.bellaouzo.eventlens.runtime.ClientScreenCloseEvent";
    public static final String CLIENT_ATTACK_EVENT = "dev.bellaouzo.eventlens.runtime.ClientAttackEvent";
    public static final String CLIENT_ATTACK_BLOCK_EVENT = "dev.bellaouzo.eventlens.runtime.ClientAttackBlockEvent";
    public static final String CLIENT_USE_ITEM_EVENT = "dev.bellaouzo.eventlens.runtime.ClientUseItemEvent";
    public static final String CLIENT_USE_BLOCK_EVENT = "dev.bellaouzo.eventlens.runtime.ClientUseBlockEvent";
    public static final String CLIENT_USE_ENTITY_EVENT = "dev.bellaouzo.eventlens.runtime.ClientUseEntityEvent";
    public static final String CLIENT_USE_EMPTY_EVENT = "dev.bellaouzo.eventlens.runtime.ClientUseEmptyEvent";
    public static final String CLIENT_PLAYER_MOVE_EVENT = "dev.bellaouzo.eventlens.runtime.ClientPlayerMoveEvent";
    public static final String CLIENT_MOVEMENT_INPUT_EVENT = "dev.bellaouzo.eventlens.runtime.ClientMovementInputEvent";
    public static final String CLIENT_JOIN_EVENT = "dev.bellaouzo.eventlens.runtime.ClientJoinEvent";
    public static final String CLIENT_DISCONNECT_EVENT = "dev.bellaouzo.eventlens.runtime.ClientDisconnectEvent";
    public static final String CLIENT_RESPAWN_EVENT = "dev.bellaouzo.eventlens.runtime.ClientRespawnEvent";
    public static final String CLIENT_GAME_TYPE_CHANGE_EVENT = "dev.bellaouzo.eventlens.runtime.ClientGameTypeChangeEvent";
    public static final String CLIENT_PAUSE_EVENT = "dev.bellaouzo.eventlens.runtime.ClientPauseEvent";
    public static final String CLIENT_KEY_EVENT = "dev.bellaouzo.eventlens.runtime.ClientKeyEvent";
    public static final String CLIENT_MOUSE_BUTTON_EVENT = "dev.bellaouzo.eventlens.runtime.ClientMouseButtonEvent";
    public static final String CLIENT_MOUSE_SCROLL_EVENT = "dev.bellaouzo.eventlens.runtime.ClientMouseScrollEvent";
    public static final String CLIENT_INTERACTION_KEY_EVENT = "dev.bellaouzo.eventlens.runtime.ClientInteractionKeyEvent";
    public static final String CLIENT_TOOLTIP_EVENT = "dev.bellaouzo.eventlens.runtime.ClientTooltipEvent";
    public static final String CLIENT_SCREENSHOT_EVENT = "dev.bellaouzo.eventlens.runtime.ClientScreenshotEvent";
    public static final String CLIENT_TOAST_EVENT = "dev.bellaouzo.eventlens.runtime.ClientToastEvent";
    public static final String CLIENT_SOUND_EVENT = "dev.bellaouzo.eventlens.runtime.ClientSoundEvent";
    public static final String CLIENT_ENTITY_JOIN_EVENT = "dev.bellaouzo.eventlens.runtime.ClientEntityJoinEvent";
    public static final String CLIENT_ENTITY_LEAVE_EVENT = "dev.bellaouzo.eventlens.runtime.ClientEntityLeaveEvent";
    public static final String CLIENT_CHUNK_LOAD_EVENT = "dev.bellaouzo.eventlens.runtime.ClientChunkLoadEvent";
    public static final String CLIENT_CHUNK_UNLOAD_EVENT = "dev.bellaouzo.eventlens.runtime.ClientChunkUnloadEvent";
    public static final String CLIENT_RECIPES_UPDATED_EVENT = "dev.bellaouzo.eventlens.runtime.ClientRecipesUpdatedEvent";
    public static final String CLIENT_ITEM_TOSS_EVENT = "dev.bellaouzo.eventlens.runtime.ClientItemTossEvent";
    public static final String CLIENT_ITEM_PICKUP_EVENT = "dev.bellaouzo.eventlens.runtime.ClientItemPickupEvent";
    public static final String CLIENT_DEATH_EVENT = "dev.bellaouzo.eventlens.runtime.ClientDeathEvent";
    public static final String CLIENT_HURT_EVENT = "dev.bellaouzo.eventlens.runtime.ClientHurtEvent";
    public static final String CLIENT_USE_ENTITY_AT_EVENT = "dev.bellaouzo.eventlens.runtime.ClientUseEntityAtEvent";
    public static final String CLIENT_USE_ITEM_FINISH_EVENT = "dev.bellaouzo.eventlens.runtime.ClientUseItemFinishEvent";
    public static final String CLIENT_CONTAINER_OPEN_EVENT = "dev.bellaouzo.eventlens.runtime.ClientContainerOpenEvent";
    public static final String CLIENT_CONTAINER_CLOSE_EVENT = "dev.bellaouzo.eventlens.runtime.ClientContainerCloseEvent";
    public static final String CLIENT_BREAK_SPEED_EVENT = "dev.bellaouzo.eventlens.runtime.ClientBreakSpeedEvent";

    private static final Map<String, EventType> BY_SIMPLE_NAME = new LinkedHashMap<>();
    private static final Map<String, EventType> BY_CLASS_NAME = new LinkedHashMap<>();

    static {
        register(new EventType("ClientTickEvent", CLIENT_TICK_EVENT, true, "Every client tick"));
        register(new EventType("ClientWorldTickEvent", CLIENT_WORLD_TICK_EVENT, true, "Every client world tick"));
        register(new EventType("ClientPlayerTickEvent", CLIENT_PLAYER_TICK_EVENT, true, "Every local player tick"));
        register(new EventType("ClientChatEvent", CLIENT_CHAT_EVENT, false, "Chat you send"));
        register(new EventType("ClientChatReceivedEvent", CLIENT_CHAT_RECEIVED_EVENT, false, "Chat you receive"));
        register(new EventType("ClientScreenOpenEvent", CLIENT_SCREEN_OPEN_EVENT, false, "A screen opened"));
        register(new EventType("ClientScreenCloseEvent", CLIENT_SCREEN_CLOSE_EVENT, false, "A screen closed"));
        register(new EventType("ClientAttackEvent", CLIENT_ATTACK_EVENT, false, "Attack an entity or empty air"));
        register(new EventType("ClientAttackBlockEvent", CLIENT_ATTACK_BLOCK_EVENT, false, "Punch a block"));
        register(new EventType("ClientUseItemEvent", CLIENT_USE_ITEM_EVENT, false, "Right-click an item"));
        register(new EventType("ClientUseBlockEvent", CLIENT_USE_BLOCK_EVENT, false, "Right-click a block"));
        register(new EventType("ClientUseEntityEvent", CLIENT_USE_ENTITY_EVENT, false, "Right-click an entity"));
        register(new EventType("ClientUseEmptyEvent", CLIENT_USE_EMPTY_EVENT, false, "Right-click empty air"));
        register(new EventType("ClientPlayerMoveEvent", CLIENT_PLAYER_MOVE_EVENT, true, "Your position changed"));
        register(new EventType("ClientMovementInputEvent", CLIENT_MOVEMENT_INPUT_EVENT, true, "Movement keys updated"));
        register(new EventType("ClientJoinEvent", CLIENT_JOIN_EVENT, false, "Joined a world"));
        register(new EventType("ClientDisconnectEvent", CLIENT_DISCONNECT_EVENT, false, "Left a world"));
        register(new EventType("ClientRespawnEvent", CLIENT_RESPAWN_EVENT, false, "Respawned"));
        register(new EventType("ClientGameTypeChangeEvent", CLIENT_GAME_TYPE_CHANGE_EVENT, false, "Game mode changed"));
        register(new EventType("ClientPauseEvent", CLIENT_PAUSE_EVENT, false, "Pause state changed"));
        register(new EventType("ClientKeyEvent", CLIENT_KEY_EVENT, false, "Keyboard press or release"));
        register(new EventType("ClientMouseButtonEvent", CLIENT_MOUSE_BUTTON_EVENT, false, "Mouse button press or release"));
        register(new EventType("ClientMouseScrollEvent", CLIENT_MOUSE_SCROLL_EVENT, false, "Mouse wheel outside a screen"));
        register(new EventType("ClientInteractionKeyEvent", CLIENT_INTERACTION_KEY_EVENT, false, "Attack, use, or pick keybind"));
        register(new EventType("ClientTooltipEvent", CLIENT_TOOLTIP_EVENT, true, "Item tooltip gathered"));
        register(new EventType("ClientScreenshotEvent", CLIENT_SCREENSHOT_EVENT, false, "Screenshot taken"));
        register(new EventType("ClientToastEvent", CLIENT_TOAST_EVENT, false, "Toast queued"));
        register(new EventType("ClientSoundEvent", CLIENT_SOUND_EVENT, false, "Sound about to play"));
        register(new EventType("ClientEntityJoinEvent", CLIENT_ENTITY_JOIN_EVENT, true, "Entity loaded on the client"));
        register(new EventType("ClientEntityLeaveEvent", CLIENT_ENTITY_LEAVE_EVENT, true, "Entity unloaded on the client"));
        register(new EventType("ClientChunkLoadEvent", CLIENT_CHUNK_LOAD_EVENT, true, "Chunk loaded on the client"));
        register(new EventType("ClientChunkUnloadEvent", CLIENT_CHUNK_UNLOAD_EVENT, true, "Chunk unloaded on the client"));
        register(new EventType("ClientRecipesUpdatedEvent", CLIENT_RECIPES_UPDATED_EVENT, false, "Recipe book synced"));
        register(new EventType("ClientItemTossEvent", CLIENT_ITEM_TOSS_EVENT, false, "You dropped an item"));
        register(new EventType("ClientItemPickupEvent", CLIENT_ITEM_PICKUP_EVENT, true, "You picked up an item"));
        register(new EventType("ClientDeathEvent", CLIENT_DEATH_EVENT, false, "A living entity died on the client"));
        register(new EventType("ClientHurtEvent", CLIENT_HURT_EVENT, true, "You took damage"));
        register(new EventType("ClientUseEntityAtEvent", CLIENT_USE_ENTITY_AT_EVENT, false, "Right-click a specific point on an entity"));
        register(new EventType("ClientUseItemFinishEvent", CLIENT_USE_ITEM_FINISH_EVENT, false, "Finished using an item"));
        register(new EventType("ClientContainerOpenEvent", CLIENT_CONTAINER_OPEN_EVENT, false, "A container menu opened"));
        register(new EventType("ClientContainerCloseEvent", CLIENT_CONTAINER_CLOSE_EVENT, false, "A container menu closed"));
        register(new EventType("ClientBreakSpeedEvent", CLIENT_BREAK_SPEED_EVENT, true, "Mining speed calculated"));
    }

    private SupportedModEventTypes() {}

    public static List<String> simpleNames() {
        return BY_SIMPLE_NAME.values().stream().map(EventType::simpleName).toList();
    }

    public static boolean isSupportedSimpleName(String simpleName) {
        return resolve(simpleName).isPresent();
    }

    public static boolean isHot(String classOrSimpleName) {
        return resolve(classOrSimpleName)
                .or(() -> Optional.ofNullable(BY_CLASS_NAME.get(classOrSimpleName)))
                .map(EventType::hot)
                .orElse(false);
    }

    public static String resolveClassName(String simpleName) {
        return resolve(simpleName).map(EventType::className).orElse(null);
    }

    public static String summary(String simpleName) {
        return resolve(simpleName).map(EventType::summary).orElse("");
    }

    public static String displaySimpleName(String className) {
        EventType type = BY_CLASS_NAME.get(className);
        if (type != null) {
            return type.simpleName();
        }
        int lastDot = className.lastIndexOf('.');
        return lastDot < 0 ? className : className.substring(lastDot + 1);
    }

    public static Optional<EventType> resolve(String simpleName) {
        if (simpleName == null || simpleName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_SIMPLE_NAME.get(simpleName.trim().toLowerCase(Locale.ROOT)));
    }

    private static void register(EventType type) {
        BY_SIMPLE_NAME.put(type.simpleName().toLowerCase(Locale.ROOT), type);
        BY_CLASS_NAME.put(type.className(), type);
    }

    public record EventType(String simpleName, String className, boolean hot, String summary) {}
}
