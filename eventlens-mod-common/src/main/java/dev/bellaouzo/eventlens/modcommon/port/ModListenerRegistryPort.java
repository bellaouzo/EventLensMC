package dev.bellaouzo.eventlens.modcommon.port;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import java.util.List;

public interface ModListenerRegistryPort {

    List<ModHandlerRegistration> listHandlers(String eventClassName);
}
