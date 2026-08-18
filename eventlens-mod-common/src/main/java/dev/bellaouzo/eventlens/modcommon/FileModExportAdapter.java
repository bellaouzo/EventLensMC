package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public final class FileModExportAdapter implements ExportPort {

    private static final String BASELINE_EXTENSION = ".baseline";

    private final Path rootDirectory;
    private final Path reportsRoot;
    private final Path baselinesRoot;

    public FileModExportAdapter(Path configDirectory) {
        this.rootDirectory = configDirectory;
        this.reportsRoot = configDirectory.resolve("reports");
        this.baselinesRoot = configDirectory.resolve("baselines");
    }

    @Override
    public Path reportsDirectory() {
        return reportsRoot;
    }

    @Override
    public Path baselinesDirectory() {
        return baselinesRoot;
    }

    @Override
    public ExportWriteResult writeReport(String safeBaseName, ExportFormat format, String content) {
        try {
            Files.createDirectories(reportsRoot);
            String fileName = sanitizeFileName(safeBaseName) + "." + format.extension();
            Path target = reportsRoot.resolve(fileName).normalize();
            if (!target.startsWith(reportsRoot)) {
                return ExportWriteResult.failure("Invalid export path.");
            }
            if (Files.exists(target)) {
                return ExportWriteResult.failure("Export file already exists.");
            }
            Path temp = Files.createTempFile(reportsRoot, "export-", ".tmp");
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
            Files.createDirectories(baselinesRoot);
            String fileName = sanitizeFileName(safeBaseName) + BASELINE_EXTENSION;
            Path target = baselinesRoot.resolve(fileName).normalize();
            if (!target.startsWith(baselinesRoot)) {
                return ExportWriteResult.failure("Invalid baseline path.");
            }
            if (Files.exists(target)) {
                return ExportWriteResult.failure("Baseline file already exists.");
            }
            Path temp = Files.createTempFile(baselinesRoot, "baseline-", ".tmp");
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
            if (!Files.isDirectory(reportsRoot)) {
                return 0;
            }
            int deleted = 0;
            try (Stream<Path> paths = Files.list(reportsRoot)) {
                for (Path path : paths.toList()) {
                    if (Files.isRegularFile(path)
                            && Files.getLastModifiedTime(path).toMillis() < cutoffMillis) {
                        Files.deleteIfExists(path);
                        deleted++;
                    }
                }
            }
            return deleted;
        } catch (IOException ex) {
            return 0;
        }
    }

    @Override
    public Optional<String> readReport(String safeBaseName, ExportFormat format) {
        Path path = reportsRoot
                .resolve(sanitizeFileName(safeBaseName) + "." + format.extension())
                .normalize();
        if (!path.startsWith(reportsRoot) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> readBaseline(String safeBaseName) {
        Path path = baselinesRoot
                .resolve(sanitizeFileName(safeBaseName) + BASELINE_EXTENSION)
                .normalize();
        if (!path.startsWith(baselinesRoot) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> listBaselines() {
        try {
            if (!Files.isDirectory(baselinesRoot)) {
                return List.of();
            }
            try (Stream<Path> paths = Files.list(baselinesRoot)) {
                return paths.filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(BASELINE_EXTENSION))
                        .map(name -> name.substring(0, name.length() - BASELINE_EXTENSION.length()))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
            }
        } catch (IOException ex) {
            return List.of();
        }
    }

    @Override
    public boolean deleteBaseline(String safeBaseName) {
        Path path = baselinesRoot
                .resolve(sanitizeFileName(safeBaseName) + BASELINE_EXTENSION)
                .normalize();
        if (!path.startsWith(baselinesRoot) || !Files.isRegularFile(path)) {
            return false;
        }
        try {
            return Files.deleteIfExists(path);
        } catch (IOException ex) {
            return false;
        }
    }

    public Path rootDirectory() {
        return rootDirectory;
    }

    private static String sanitizeFileName(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
    }
}
