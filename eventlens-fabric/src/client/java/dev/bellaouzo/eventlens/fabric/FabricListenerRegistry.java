package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.port.ModListenerRegistryPort;
import java.util.List;

final class FabricListenerRegistry implements ModListenerRegistryPort {

    @Override
    public List<ModHandlerRegistration> listHandlers(String eventClassName) {
        return List.of();
    }
}
