package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import dev.bellaouzo.eventlens.modcommon.port.ModListenerRegistryPort;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

final class FabricListenerRegistry implements ModListenerRegistryPort {

    @Override
    public List<ModHandlerRegistration> listHandlers(String eventClassName) {
        if (SupportedModEventTypes.displaySimpleName(eventClassName).isBlank()) {
            return List.of();
        }
        List<ModHandlerRegistration> handlers = new ArrayList<>();
        handlers.add(new ModHandlerRegistration("eventlens", "dev.bellaouzo.eventlens.fabric.FabricEventTracer", "record", 0));
        FabricLoader.getInstance().getAllMods().stream()
                .map(mod -> mod.getMetadata().getId())
                .filter(id -> !"eventlens".equals(id) && !"minecraft".equals(id) && !"java".equals(id))
                .limit(8)
                .forEach(id -> handlers.add(new ModHandlerRegistration(id, "fabric.callback", "unknown", 0)));
        return List.copyOf(handlers);
    }
}
