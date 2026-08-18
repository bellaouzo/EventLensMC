package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;

final class ForgeEventTypes {

    private ForgeEventTypes() {}

    static String syntheticClassName(String platformType) {
        String name = platformType.replace('/', '.');
        if (contains(name, "ClientTickEvent")) {
            return SupportedModEventTypes.CLIENT_TICK_EVENT;
        }
        if (contains(name, "LevelTickEvent")) {
            return SupportedModEventTypes.CLIENT_WORLD_TICK_EVENT;
        }
        if (contains(name, "PlayerTickEvent")) {
            return SupportedModEventTypes.CLIENT_PLAYER_TICK_EVENT;
        }
        if (contains(name, "ClientChatReceivedEvent")) {
            return SupportedModEventTypes.CLIENT_CHAT_RECEIVED_EVENT;
        }
        if (contains(name, "ClientChatEvent")) {
            return SupportedModEventTypes.CLIENT_CHAT_EVENT;
        }
        if (contains(name, "ScreenEvent$Opening") || contains(name, "ScreenEvent.Opening")) {
            return SupportedModEventTypes.CLIENT_SCREEN_OPEN_EVENT;
        }
        if (contains(name, "ScreenEvent$Closing") || contains(name, "ScreenEvent.Closing")) {
            return SupportedModEventTypes.CLIENT_SCREEN_CLOSE_EVENT;
        }
        if (contains(name, "AttackEntityEvent") || contains(name, "LeftClickEmpty")) {
            return SupportedModEventTypes.CLIENT_ATTACK_EVENT;
        }
        if (contains(name, "LeftClickBlock")) {
            return SupportedModEventTypes.CLIENT_ATTACK_BLOCK_EVENT;
        }
        if (contains(name, "RightClickEmpty")) {
            return SupportedModEventTypes.CLIENT_USE_EMPTY_EVENT;
        }
        if (contains(name, "RightClickItem")) {
            return SupportedModEventTypes.CLIENT_USE_ITEM_EVENT;
        }
        if (contains(name, "RightClickBlock")) {
            return SupportedModEventTypes.CLIENT_USE_BLOCK_EVENT;
        }
        if (contains(name, "EntityInteractSpecific")) {
            return SupportedModEventTypes.CLIENT_USE_ENTITY_AT_EVENT;
        }
        if (contains(name, "EntityInteract")) {
            return SupportedModEventTypes.CLIENT_USE_ENTITY_EVENT;
        }
        if (contains(name, "ItemTossEvent")) {
            return SupportedModEventTypes.CLIENT_ITEM_TOSS_EVENT;
        }
        if (contains(name, "ItemEntityPickupEvent") || contains(name, "EntityItemPickupEvent")) {
            return SupportedModEventTypes.CLIENT_ITEM_PICKUP_EVENT;
        }
        if (contains(name, "LivingDeathEvent")) {
            return SupportedModEventTypes.CLIENT_DEATH_EVENT;
        }
        if (contains(name, "LivingIncomingDamageEvent") || contains(name, "LivingHurtEvent")) {
            return SupportedModEventTypes.CLIENT_HURT_EVENT;
        }
        if (contains(name, "LivingEntityUseItemEvent")) {
            return SupportedModEventTypes.CLIENT_USE_ITEM_FINISH_EVENT;
        }
        if (contains(name, "PlayerContainerEvent$Open") || contains(name, "PlayerContainerEvent.Open")) {
            return SupportedModEventTypes.CLIENT_CONTAINER_OPEN_EVENT;
        }
        if (contains(name, "PlayerContainerEvent$Close") || contains(name, "PlayerContainerEvent.Close")) {
            return SupportedModEventTypes.CLIENT_CONTAINER_CLOSE_EVENT;
        }
        if (contains(name, "BreakSpeed")) {
            return SupportedModEventTypes.CLIENT_BREAK_SPEED_EVENT;
        }
        if (contains(name, "MovementInputUpdateEvent")) {
            return SupportedModEventTypes.CLIENT_MOVEMENT_INPUT_EVENT;
        }
        if (contains(name, "LoggingIn")) {
            return SupportedModEventTypes.CLIENT_JOIN_EVENT;
        }
        if (contains(name, "LoggingOut")) {
            return SupportedModEventTypes.CLIENT_DISCONNECT_EVENT;
        }
        if (contains(name, "ClientPlayerNetworkEvent$Clone") || contains(name, "ClientPlayerNetworkEvent.Clone")) {
            return SupportedModEventTypes.CLIENT_RESPAWN_EVENT;
        }
        if (contains(name, "ClientPlayerChangeGameTypeEvent")) {
            return SupportedModEventTypes.CLIENT_GAME_TYPE_CHANGE_EVENT;
        }
        if (contains(name, "ClientPauseChangeEvent")) {
            return SupportedModEventTypes.CLIENT_PAUSE_EVENT;
        }
        if (contains(name, "InteractionKeyMappingTriggered")) {
            return SupportedModEventTypes.CLIENT_INTERACTION_KEY_EVENT;
        }
        if (contains(name, "MouseScrollingEvent")) {
            return SupportedModEventTypes.CLIENT_MOUSE_SCROLL_EVENT;
        }
        if (contains(name, "InputEvent$MouseButton") || contains(name, "InputEvent.MouseButton")) {
            return SupportedModEventTypes.CLIENT_MOUSE_BUTTON_EVENT;
        }
        if (contains(name, "InputEvent$Key") || contains(name, "InputEvent.Key")) {
            return SupportedModEventTypes.CLIENT_KEY_EVENT;
        }
        if (contains(name, "ItemTooltipEvent") || contains(name, "RenderTooltipEvent")) {
            return SupportedModEventTypes.CLIENT_TOOLTIP_EVENT;
        }
        if (contains(name, "ScreenshotEvent")) {
            return SupportedModEventTypes.CLIENT_SCREENSHOT_EVENT;
        }
        if (contains(name, "ToastAddEvent")) {
            return SupportedModEventTypes.CLIENT_TOAST_EVENT;
        }
        if (contains(name, "PlaySoundEvent") || contains(name, "PlayLevelSoundEvent")) {
            return SupportedModEventTypes.CLIENT_SOUND_EVENT;
        }
        if (contains(name, "EntityLeaveLevelEvent")) {
            return SupportedModEventTypes.CLIENT_ENTITY_LEAVE_EVENT;
        }
        if (contains(name, "EntityJoinLevelEvent")) {
            return SupportedModEventTypes.CLIENT_ENTITY_JOIN_EVENT;
        }
        if (contains(name, "ChunkEvent$Unload") || contains(name, "ChunkEvent.Unload")) {
            return SupportedModEventTypes.CLIENT_CHUNK_UNLOAD_EVENT;
        }
        if (contains(name, "ChunkEvent$Load") || contains(name, "ChunkEvent.Load")) {
            return SupportedModEventTypes.CLIENT_CHUNK_LOAD_EVENT;
        }
        if (contains(name, "RecipesUpdatedEvent")) {
            return SupportedModEventTypes.CLIENT_RECIPES_UPDATED_EVENT;
        }
        return null;
    }

    private static boolean contains(String name, String token) {
        return name.contains(token);
    }
}
