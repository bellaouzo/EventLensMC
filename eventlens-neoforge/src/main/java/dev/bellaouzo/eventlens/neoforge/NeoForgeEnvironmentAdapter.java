package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import dev.bellaouzo.eventlens.modcommon.port.ModEnvironmentPort;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public final class NeoForgeEnvironmentAdapter implements ModEnvironmentPort {

    private final ModContainer modContainer;

    public NeoForgeEnvironmentAdapter(ModContainer modContainer) {
        this.modContainer = modContainer;
    }

    @Override
    public ModRuntimeKind runtimeKind() {
        return ModRuntimeKind.NEOFORGE;
    }

    @Override
    public String loaderVersion() {
        return ModList.get()
                .getModContainerById("neoforge")
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public Map<String, String> loadedModVersions() {
        Map<String, String> versions = new LinkedHashMap<>();
        ModList.get().getMods().forEach(modInfo -> {
            if (versions.size() >= 64) {
                return;
            }
            versions.put(modInfo.getModId(), modInfo.getVersion().toString());
        });
        return versions;
    }

    @Override
    public String platformLabel() {
        return "NeoForge " + loaderVersion();
    }

    @Override
    public String minecraftVersion() {
        return SharedConstants.getCurrentVersion().name();
    }

    @Override
    public String eventLensVersion() {
        return modContainer.getModInfo().getVersion().toString();
    }
}
