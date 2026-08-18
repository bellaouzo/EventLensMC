package dev.bellaouzo.eventlens.setup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class DestinationResolver {

    private DestinationResolver() {}

    static Path payloadDirectory(SetupTarget target, Path selected) {
        Path folder = selected.toAbsolutePath().normalize();
        if (target.paper()) {
            return namedOrParent(folder, "plugins");
        }
        Path instanceMods =
                prismMinecraftDir(folder).map(dir -> dir.resolve("mods")).orElse(null);
        if (instanceMods != null) {
            return instanceMods;
        }
        return namedOrParent(folder, "mods");
    }

    static Path serverRoot(Path selected) {
        Path folder = selected.toAbsolutePath().normalize();
        if (folder.getFileName() != null
                && "plugins".equalsIgnoreCase(folder.getFileName().toString())) {
            return folder.getParent() == null ? folder : folder.getParent();
        }
        return folder;
    }

    static Optional<Path> prismInstanceRoot(Path selected) {
        Path current = selected.toAbsolutePath().normalize();
        for (int depth = 0; depth < 5 && current != null; depth++) {
            if (Files.isRegularFile(current.resolve("instance.cfg"))) {
                return Optional.of(current);
            }
            current = current.getParent();
        }
        return Optional.empty();
    }

    static Optional<Path> prismMinecraftDir(Path selected) {
        return prismInstanceRoot(selected).map(root -> {
            Path hidden = root.resolve(".minecraft");
            if (Files.isDirectory(hidden)) {
                return hidden;
            }
            Path plain = root.resolve("minecraft");
            if (Files.isDirectory(plain)) {
                return plain;
            }
            return hidden;
        });
    }

    public static String describe(SetupTarget target, Path selected) {
        if (target.paper()) {
            return "Plugin will go in " + payloadDirectory(target, selected);
        }
        Optional<Path> prism = prismInstanceRoot(selected);
        if (prism.isPresent()) {
            return "Prism/MultiMC instance: " + prism.get().getFileName() + " — mod goes in "
                    + payloadDirectory(target, selected);
        }
        return "Mod will go in " + payloadDirectory(target, selected);
    }

    private static Path namedOrParent(Path folder, String childName) {
        if (folder.getFileName() != null
                && childName.equalsIgnoreCase(folder.getFileName().toString())) {
            return folder;
        }
        return folder.resolve(childName);
    }
}
