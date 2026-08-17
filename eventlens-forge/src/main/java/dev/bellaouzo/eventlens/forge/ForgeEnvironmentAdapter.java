package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import dev.bellaouzo.eventlens.modcommon.port.ModEnvironmentPort;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.versions.forge.ForgeVersion;

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
        return ForgeVersion.getVersion();
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
        return FMLLoader.versionInfo().mcVersion();
    }

    @Override
    public String eventLensVersion() {
        return context.getContainer().getModInfo().getVersion().toString();
    }
}
