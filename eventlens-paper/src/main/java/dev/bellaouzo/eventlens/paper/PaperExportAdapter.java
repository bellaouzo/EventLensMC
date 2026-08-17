package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.bukkit.plugin.Plugin;

public final class PaperExportAdapter implements ExportPort {

    private static final String BASELINE_EXTENSION = ".baseline";

    private final Plugin plugin;
    private final Path reportsDirectory;
    private final Path baselinesDirectory;

    public PaperExportAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.reportsDirectory = plugin.getDataFolder().toPath().resolve("reports");
        this.baselinesDirectory = plugin.getDataFolder().toPath().resolve("baselines");
    }

    @Override
    public Path reportsDirectory() {
        return reportsDirectory;
    }

    @Override
    public Path baselinesDirectory() {
        return baselinesDirectory;
    }

    @Override
    public ExportWriteResult writeReport(String safeBaseName, ExportFormat format, String content) {
        try {
            Files.createDirectories(reportsDirectory);
            String fileName = sanitizeFileName(safeBaseName) + "." + format.extension();
            Path target = reportsDirectory.resolve(fileName).normalize();
            if (!target.startsWith(reportsDirectory)) {
                return ExportWriteResult.failure("Invalid export path.");
            }
            if (Files.exists(target)) {
                return ExportWriteResult.failure("Export file already exists.");
            }
            Path temp = Files.createTempFile(reportsDirectory, "export-", ".tmp");
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            return ExportWriteResult.success(target);
        } catch (IOException ex) {
            return ExportWriteResult.failure(ex.getMessage() == null ? "I/O error writing export." : ex.getMessage());
        }
    }

    @Override
    public ExportWriteResult writeBaseline(String safeBaseName, String content) {
        try {
            Files.createDirectories(baselinesDirectory);
            String fileName = sanitizeFileName(safeBaseName) + BASELINE_EXTENSION;
            Path target = baselinesDirectory.resolve(fileName).normalize();
            if (!target.startsWith(baselinesDirectory)) {
                return ExportWriteResult.failure("Invalid baseline path.");
            }
            if (Files.exists(target)) {
                return ExportWriteResult.failure("Baseline file already exists.");
            }
            Path temp = Files.createTempFile(baselinesDirectory, "baseline-", ".tmp");
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            return ExportWriteResult.success(target);
        } catch (IOException ex) {
            return ExportWriteResult.failure(ex.getMessage() == null ? "I/O error writing baseline." : ex.getMessage());
        }
    }

    @Override
    public int deleteReportsOlderThan(long cutoffMillis) {
        try {
            if (!Files.isDirectory(reportsDirectory)) {
                return 0;
            }
            int deleted = 0;
            try (Stream<Path> paths = Files.list(reportsDirectory)) {
                for (Path path : paths.toList()) {
                    if (!Files.isRegularFile(path)) {
                        continue;
                    }
                    if (Files.getLastModifiedTime(path).toMillis() < cutoffMillis) {
                        Files.deleteIfExists(path);
                        deleted++;
                    }
                }
            }
            return deleted;
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to clean up old reports: " + ex.getMessage());
            return 0;
        }
    }

    @Override
    public Optional<String> readReport(String safeBaseName, ExportFormat format) {
        Path path = reportsDirectory
                .resolve(sanitizeFileName(safeBaseName) + "." + format.extension())
                .normalize();
        if (!path.startsWith(reportsDirectory) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException _) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> readBaseline(String safeBaseName) {
        Path path = baselinesDirectory
                .resolve(sanitizeFileName(safeBaseName) + BASELINE_EXTENSION)
                .normalize();
        if (!path.startsWith(baselinesDirectory) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException _) {
            return Optional.empty();
        }
    }

    @Override
    public java.util.List<String> listBaselines() {
        try {
            if (!Files.isDirectory(baselinesDirectory)) {
                return java.util.List.of();
            }
            try (Stream<Path> paths = Files.list(baselinesDirectory)) {
                return paths.filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(BASELINE_EXTENSION))
                        .map(name -> name.substring(0, name.length() - BASELINE_EXTENSION.length()))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
            }
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to list baselines: " + ex.getMessage());
            return java.util.List.of();
        }
    }

    @Override
    public boolean deleteBaseline(String safeBaseName) {
        Path path = baselinesDirectory
                .resolve(sanitizeFileName(safeBaseName) + BASELINE_EXTENSION)
                .normalize();
        if (!path.startsWith(baselinesDirectory) || !Files.isRegularFile(path)) {
            return false;
        }
        try {
            return Files.deleteIfExists(path);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to delete baseline " + safeBaseName + ": " + ex.getMessage());
            return false;
        }
    }

    private static String sanitizeFileName(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
    }
}
