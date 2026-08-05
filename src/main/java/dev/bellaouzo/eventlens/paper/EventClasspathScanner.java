package dev.bellaouzo.eventlens.paper;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class EventClasspathScanner {

    private static final String[] EVENT_PACKAGE_PREFIXES = {
        "org/bukkit/event/", "io/papermc/paper/event/", "com/destroystokyo/paper/event/", "org/spigotmc/event/"
    };

    private EventClasspathScanner() {}

    static void scanClassLoader(ClassLoader loader, Consumer<String> classNameConsumer) {
        Set<String> visitedLoaders = new HashSet<>();
        ClassLoader current = loader;

        while (current != null) {
            String loaderKey = current.getClass().getName() + "@" + System.identityHashCode(current);
            if (!visitedLoaders.add(loaderKey)) {
                current = current.getParent();
                continue;
            }

            if (current instanceof URLClassLoader urlClassLoader) {
                for (URL url : urlClassLoader.getURLs()) {
                    scanUrl(url, classNameConsumer);
                }
            }

            current = current.getParent();
        }
    }

    private static void scanUrl(URL url, Consumer<String> classNameConsumer) {
        try {
            if ("file".equals(url.getProtocol())) {
                Path path = Path.of(url.toURI());
                if (Files.isDirectory(path)) {
                    scanDirectory(path, "", classNameConsumer);
                } else if (path.toString().endsWith(".jar")) {
                    scanJar(path, classNameConsumer);
                }
                return;
            }

            if ("jar".equals(url.getProtocol())) {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                try (JarFile jarFile = connection.getJarFile()) {
                    scanJarEntries(jarFile, classNameConsumer);
                }
            }
        } catch (Exception _) {
            // Skip unreadable classpath entries.
        }
    }

    private static void scanDirectory(Path root, String packagePath, Consumer<String> classNameConsumer)
            throws IOException {
        try (var paths = Files.list(root)) {
            for (Path entry : paths.toList()) {
                String entryName = entry.getFileName().toString();
                if (Files.isDirectory(entry)) {
                    scanDirectory(entry, packagePath + entryName + "/", classNameConsumer);
                } else if (entryName.endsWith(".class") && isEventResource(packagePath + entryName)) {
                    classNameConsumer.accept((packagePath + entryName)
                            .replace('/', '.')
                            .substring(0, packagePath.length() + entryName.length() - 6));
                }
            }
        }
    }

    private static void scanJar(Path jarPath, Consumer<String> classNameConsumer) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            scanJarEntries(jarFile, classNameConsumer);
        }
    }

    private static void scanJarEntries(JarFile jarFile, Consumer<String> classNameConsumer) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                continue;
            }

            if (isEventResource(entry.getName())) {
                String className = entry.getName()
                        .replace('/', '.')
                        .substring(0, entry.getName().length() - 6);
                classNameConsumer.accept(className);
            }
        }
    }

    private static boolean isEventResource(String resourceName) {
        for (String prefix : EVENT_PACKAGE_PREFIXES) {
            if (resourceName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
