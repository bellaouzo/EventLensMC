package dev.bellaouzo.eventlens.modcommon.port;

import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import java.util.Map;

public interface ModEnvironmentPort {

    ModRuntimeKind runtimeKind();

    String loaderVersion();

    Map<String, String> loadedModVersions();

    String platformLabel();

    String minecraftVersion();

    String eventLensVersion();
}
