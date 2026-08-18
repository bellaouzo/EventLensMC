package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.ExportPort;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class PaperBundleExporter {

    private static final String DASHBOARD_PREFIX = "dashboard/";
    private static final String INDEX_HTML = "index.html";

    private PaperBundleExporter() {}

    static ExportPort.ExportWriteResult write(Path reportsDirectory, String safeBaseName, String reportJson) {
        try {
            Files.createDirectories(reportsDirectory);
            Path target = reportsDirectory.resolve(safeBaseName + "-bundle").normalize();
            if (!target.startsWith(reportsDirectory)) {
                return ExportPort.ExportWriteResult.failure("Invalid export path.");
            }
            Files.createDirectories(target);
            Files.writeString(target.resolve("report.json"), reportJson, StandardCharsets.UTF_8);
            Files.writeString(
                    target.resolve("report.js"),
                    "window.__EVENTLENS_REPORT__=" + reportJson + ";\n",
                    StandardCharsets.UTF_8);
            copyDashboard(target);
            rewriteIndex(target.resolve(INDEX_HTML));
            return ExportPort.ExportWriteResult.success(target);
        } catch (IOException | URISyntaxException | IllegalArgumentException ex) {
            return ExportPort.ExportWriteResult.failure(
                    ex.getMessage() == null ? "I/O error writing bundle." : ex.getMessage());
        }
    }

    private static void copyDashboard(Path target) throws IOException, URISyntaxException {
        URL index = PaperBundleExporter.class.getClassLoader().getResource(DASHBOARD_PREFIX + INDEX_HTML);
        if (index == null) {
            writeFallback(target);
            return;
        }
        if ("jar".equalsIgnoreCase(index.getProtocol())) {
            copyFromJar(index, target);
            return;
        }
        Path dashboard = Path.of(index.toURI()).getParent();
        copyTree(dashboard, target);
    }

    static void copyFromJar(URL index, Path target) throws IOException, URISyntaxException {
        Path jarPath = jarFilePath(index);
        try (ZipFile zip = new ZipFile(jarPath.toFile())) {
            var entries = zip.stream()
                    .filter(entry -> !entry.isDirectory() && entry.getName().startsWith(DASHBOARD_PREFIX))
                    .toList();
            if (entries.isEmpty()) {
                writeFallback(target);
                return;
            }
            for (ZipEntry entry : entries) {
                Path dest = target.resolve(entry.getName().substring(DASHBOARD_PREFIX.length()));
                Files.createDirectories(dest.getParent());
                try (InputStream input = zip.getInputStream(entry)) {
                    Files.copy(input, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    static Path jarFilePath(URL index) throws URISyntaxException {
        String raw = index.toURI().getRawSchemeSpecificPart();
        int bang = raw.indexOf('!');
        return Path.of(URI.create(bang < 0 ? raw : raw.substring(0, bang)));
    }

    private static void copyTree(Path source, Path target) throws IOException {
        try (var walk = Files.walk(source)) {
            for (Path file : walk.toList()) {
                Path dest = target.resolve(source.relativize(file).toString());
                if (Files.isDirectory(file)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void rewriteIndex(Path index) throws IOException {
        if (!Files.isRegularFile(index)) {
            return;
        }
        String html = Files.readString(index, StandardCharsets.UTF_8)
                .replace("type=\"module\"", "")
                .replace(" crossorigin", "");
        if (!html.contains("report.js")) {
            html = html.replaceFirst("<head>", "<head>\n    <script src=\"./report.js\"></script>");
        }
        Files.writeString(index, html, StandardCharsets.UTF_8);
    }

    private static void writeFallback(Path target) throws IOException {
        Files.writeString(
                target.resolve(INDEX_HTML),
                "<!doctype html><meta charset=utf-8><title>EventLens</title>"
                        + "<p>Open report.json in the EventLens viewer.</p>",
                StandardCharsets.UTF_8);
    }
}
