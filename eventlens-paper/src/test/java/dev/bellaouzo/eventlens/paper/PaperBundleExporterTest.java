package dev.bellaouzo.eventlens.paper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PaperBundleExporterTest {

    @Test
    void copiesDashboardFromJarPathThatContainsSpaces(@TempDir Path tempDir) throws Exception {
        Path spacedDir = Files.createDirectories(tempDir.resolve("Code Projects"));
        Path jar = spacedDir.resolve("EventLens.jar");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(jar))) {
            zip.putNextEntry(new ZipEntry("dashboard/index.html"));
            zip.write("<html></html>".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("dashboard/assets/index.js"));
            zip.write("window.ready=true;".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        Path target = Files.createDirectories(tempDir.resolve("bundle"));
        PaperBundleExporter.copyFromJar(
                URI.create("jar:" + jar.toUri() + "!/dashboard/index.html").toURL(), target);

        assertTrue(Files.isRegularFile(target.resolve("index.html")));
        assertTrue(Files.isRegularFile(target.resolve("assets").resolve("index.js")));
    }
}
