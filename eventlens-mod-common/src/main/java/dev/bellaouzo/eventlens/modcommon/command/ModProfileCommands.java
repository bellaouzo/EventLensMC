package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.util.ArrayList;
import java.util.List;

final class ModProfileCommands {

    private ModProfileCommands() {}

    static List<ModChatLine> handle(ModTraceCoordinator coordinator, List<String> args) {
        if (args.size() < 2) {
            return List.of(ModChatLine.text("Usage: /eventlens mod <id> | compare <a> <b>", ModChatColor.YELLOW));
        }
        if ("compare".equalsIgnoreCase(args.get(1)) && args.size() >= 4) {
            return List.of(ModChatLine.text(
                    args.get(2) + " vs " + args.get(3) + " — compare uses @SubscribeEvent rows only.",
                    ModChatColor.GRAY));
        }
        String modId = args.get(1);
        int handlers = 0;
        for (String event : SupportedModEventTypes.simpleNames()) {
            String className = SupportedModEventTypes.resolveClassName(event);
            if (className == null) {
                continue;
            }
            for (ModHandlerRegistration handler : coordinator.listenerRegistryPort().listHandlers(className)) {
                if (modId.equalsIgnoreCase(handler.modId())) {
                    handlers++;
                }
            }
        }
        List<ModChatLine> lines = new ArrayList<>();
        lines.add(ModChatLine.text("Mod " + modId, ModChatColor.GOLD));
        lines.add(ModChatLine.text(handlers + " scanned @SubscribeEvent handler(s).", ModChatColor.GRAY));
        lines.add(ModChatLine.text("addListener() consumers stay invisible without the client agent.", ModChatColor.DARK_GRAY));
        return lines;
    }

    static List<ModChatLine> exceptions(ModTraceCoordinator coordinator) {
        return List.of(ModChatLine.text(
                "Exception inbox is session-scoped. Use /eventlens trace view after a throw.",
                ModChatColor.YELLOW));
    }
}
