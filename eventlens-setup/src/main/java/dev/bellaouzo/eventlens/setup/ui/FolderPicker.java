package dev.bellaouzo.eventlens.setup.ui;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.swing.JFileChooser;

final class FolderPicker {

    private FolderPicker() {}

    static Optional<Path> choose(Component owner, Path current) {
        if (windows()) {
            NativePick nativePick = windowsExplorer(current);
            if (nativePick.launched()) {
                return nativePick.path();
            }
        }
        return swingChooser(owner, current);
    }

    static boolean windows() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase(Locale.ROOT).contains("win");
    }

    static Optional<Path> parseOutput(String stdout) {
        if (stdout == null || stdout.isBlank()) {
            return Optional.empty();
        }
        Optional<String> last =
                stdout.lines().map(String::trim).filter(line -> !line.isEmpty()).reduce((first, second) -> second);
        if (last.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Path.of(last.get()));
    }

    private static NativePick windowsExplorer(Path current) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    powershell(),
                    "-NoLogo",
                    "-NoProfile",
                    "-STA",
                    "-WindowStyle",
                    "Hidden",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-EncodedCommand",
                    encodedScript());
            builder.environment().put("EVENTLENS_FOLDER_START", startPath(current));
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = builder.start();
            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!process.waitFor(5, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                return NativePick.failed();
            }
            if (process.exitValue() != 0) {
                return NativePick.failed();
            }
            return NativePick.launched(parseOutput(stdout));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return NativePick.failed();
        } catch (IOException ex) {
            return NativePick.failed();
        }
    }

    record NativePick(boolean launched, Optional<Path> path) {

        static NativePick launched(Optional<Path> path) {
            return new NativePick(true, path);
        }

        static NativePick failed() {
            return new NativePick(false, Optional.empty());
        }
    }

    private static Optional<Path> swingChooser(Component owner, Path current) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (current != null && Files.isDirectory(current)) {
            chooser.setCurrentDirectory(current.toFile());
        }
        if (chooser.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        return Optional.of(chooser.getSelectedFile().toPath());
    }

    private static String startPath(Path current) {
        if (current != null && Files.isDirectory(current)) {
            return current.toAbsolutePath().normalize().toString();
        }
        return "";
    }

    private static String powershell() {
        String root = System.getenv("SystemRoot");
        if (root != null && !root.isBlank()) {
            return Path.of(root, "System32", "WindowsPowerShell", "v1.0", "powershell.exe")
                    .toString();
        }
        return "powershell.exe";
    }

    private static String encodedScript() throws IOException {
        try (InputStream in = FolderPicker.class.getResourceAsStream("folder-picker.ps1")) {
            if (in == null) {
                throw new IOException("Missing folder-picker.ps1");
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        }
    }
}
