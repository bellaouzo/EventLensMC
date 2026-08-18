package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import dev.bellaouzo.eventlens.modcommon.port.ModEnvironmentPort;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ForgeEnvironmentAdapter implements ModEnvironmentPort {

    private final FMLJavaModLoadingContext context;

    public ForgeEnvironmentAdapter(FMLJavaModLoadingContext context) {
        this.context = context;
    }

    @Override
    public ModRuntimeKind runtimeKind() {
        return ModRuntimeKind.FORGE;
    }

    @Override
    public String loaderVersion() {
        return ModList.get()
                .getModContainerById("forge")
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
        return "Forge " + loaderVersion();
    }

    @Override
    public String minecraftVersion() {
        return SharedConstants.getCurrentVersion().name();
    }

    @Override
    public String eventLensVersion() {
        return context.getContainer().getModInfo().getVersion().toString();
    }
}
