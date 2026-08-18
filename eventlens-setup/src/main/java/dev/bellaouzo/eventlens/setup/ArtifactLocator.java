package dev.bellaouzo.eventlens.setup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class ArtifactLocator {

    private final String version;
    private final Path extraDirectory;

    public ArtifactLocator(String version, Path extraDirectory) {
        this.version = version;
        this.extraDirectory = extraDirectory;
    }

    public static ArtifactLocator create(String version) {
        return new ArtifactLocator(
                version,
                extraFromProperty().or(ArtifactLocator::besideRunningJar).orElse(null));
    }

    Optional<Path> find(String fileName) {
        if (extraDirectory != null) {
            Path beside = extraDirectory.resolve(fileName);
            if (Files.isRegularFile(beside)) {
                return Optional.of(beside);
            }
        }
        return Optional.empty();
    }

    Optional<InputStream> openEmbedded(String fileName) {
        return Optional.ofNullable(EventLensSetup.class.getResourceAsStream("/setup-artifacts/" + fileName));
    }

    void copyTo(String fileName, Path destination) throws IOException {
        Optional<Path> found = find(fileName);
        if (found.isPresent()) {
            Files.copy(found.get(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        Optional<InputStream> embedded = openEmbedded(fileName);
        if (embedded.isEmpty()) {
            throw new IOException("Could not find " + fileName + " next to the setup jar or inside it. "
                    + "Download the matching " + version + " files from GitHub Releases.");
        }
        try (InputStream in = embedded.get()) {
            Files.copy(in, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Optional<Path> extraFromProperty() {
        String property = System.getProperty("eventlens.setup.artifactsDir");
        if (property == null || property.isBlank()) {
            return Optional.empty();
        }
        Path dir = Path.of(property);
        return Files.isDirectory(dir) ? Optional.of(dir) : Optional.empty();
    }

    private static Optional<Path> besideRunningJar() {
        try {
            URI location = EventLensSetup.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            Path path = Path.of(location);
            if (Files.isRegularFile(path) && path.getParent() != null) {
                return Optional.of(path.getParent());
            }
            if (Files.isDirectory(path)) {
                return Optional.of(path);
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
