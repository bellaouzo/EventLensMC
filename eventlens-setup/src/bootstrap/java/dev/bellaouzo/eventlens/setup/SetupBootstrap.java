package dev.bellaouzo.eventlens.setup;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Entry point compiled for Java 8 so double-clicking with "Java SE Platform Binary" can explain
 * itself or relaunch a newer OpenJDK instead of dying with UnsupportedClassVersionError.
 */
public final class SetupBootstrap {

    private static final Logger LOG = Logger.getLogger(SetupBootstrap.class.getName());

    private SetupBootstrap() {}

    public static void main(String[] args) {
        if (JavaRuntimes.isNewEnough(JavaRuntimes.runtimeMajor())) {
            launchWizard(args);
            return;
        }
        File newer = JavaRuntimes.findNewerJavaw();
        File self = currentJar();
        if (newer != null && self != null && self.isFile()) {
            try {
                new ProcessBuilder(newer.getAbsolutePath(), "-jar", self.getAbsolutePath())
                        .directory(self.getParentFile())
                        .start();
                return;
            } catch (IOException ignored) {
                // Fall through to the instructions dialog.
            }
        }
        explainRequiredRuntime();
    }

    private static void launchWizard(String[] args) {
        try {
            Class<?> main = Class.forName("dev.bellaouzo.eventlens.setup.EventLensSetup");
            main.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (ClassNotFoundException
                | NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException ex) {
            explain("Could not start EventLens setup:\n" + ex);
        }
    }

    private static File currentJar() {
        try {
            URI uri = SetupBootstrap.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            File file = new File(uri);
            return file.isFile() ? file : null;
        } catch (URISyntaxException | RuntimeException ignored) {
            return null;
        }
    }

    private static void explainRequiredRuntime() {
        explain(JavaRuntimes.requiredRuntimeMessage());
    }

    private static void explain(String message) {
        LOG.severe(message);
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null, message, "EventLens setup", JOptionPane.ERROR_MESSAGE);
        }
    }
}
