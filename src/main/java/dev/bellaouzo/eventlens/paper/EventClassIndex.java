package dev.bellaouzo.eventlens.paper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

final class EventClassIndex {

    private final ClassLoader classLoader;
    private final Map<String, Class<? extends Event>> bySimpleName = new ConcurrentHashMap<>();
    private final List<Class<? extends Event>> allEvents = new ArrayList<>();
    private volatile boolean indexed;

    EventClassIndex(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    void ensureIndexed() {
        if (indexed) {
            return;
        }

        synchronized (this) {
            if (indexed) {
                return;
            }

            EventClasspathScanner.scanClassLoader(classLoader, this::registerClassName);
            indexed = true;
        }
    }

    List<Class<? extends Event>> findMatches(String query) {
        ensureIndexed();

        String normalized = query.trim();
        if (normalized.isEmpty()) {
            return List.of();
        }

        Class<? extends Event> exact = resolveExact(normalized);
        if (exact != null) {
            return List.of(exact);
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        return allEvents.stream()
                .filter(eventClass -> matchesPartial(eventClass, lower))
                .sorted(Comparator.comparing(Class::getSimpleName))
                .toList();
    }

    List<String> listSimpleNames() {
        ensureIndexed();
        return allEvents.stream()
                .map(Class::getSimpleName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    List<String> listClassNames() {
        ensureIndexed();
        return allEvents.stream()
                .map(Class::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    Class<? extends Event> resolveClassName(String className) {
        ensureIndexed();

        Class<? extends Event> exact = resolveExact(className);
        if (exact != null) {
            return exact;
        }

        throw new IllegalArgumentException("Unknown event class: " + className);
    }

    private Class<? extends Event> resolveExact(String query) {
        Class<? extends Event> byName = bySimpleName.get(query.toLowerCase(Locale.ROOT));
        if (byName != null) {
            return byName;
        }

        if (!query.endsWith("Event")) {
            byName = bySimpleName.get((query + "Event").toLowerCase(Locale.ROOT));
            if (byName != null) {
                return byName;
            }
        }

        try {
            Class<?> loaded = Class.forName(query, false, classLoader);
            if (Event.class.isAssignableFrom(loaded) && !Modifier.isAbstract(loaded.getModifiers())) {
                @SuppressWarnings("unchecked")
                Class<? extends Event> eventClass = (Class<? extends Event>) loaded;
                registerEventClass(eventClass);
                return eventClass;
            }
        } catch (ClassNotFoundException _) {
            return null;
        }

        return null;
    }

    private boolean matchesPartial(Class<? extends Event> eventClass, String lowerQuery) {
        String simpleName = eventClass.getSimpleName().toLowerCase(Locale.ROOT);
        String qualifiedName = eventClass.getName().toLowerCase(Locale.ROOT);
        return simpleName.contains(lowerQuery) || qualifiedName.contains(lowerQuery);
    }

    private void registerClassName(String className) {
        try {
            Class<?> loaded = Class.forName(className, false, classLoader);
            if (!Event.class.isAssignableFrom(loaded) || Modifier.isAbstract(loaded.getModifiers())) {
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) loaded;
            registerEventClass(eventClass);
        } catch (ClassNotFoundException | LinkageError _) {
            // Skip unloadable classes.
        }
    }

    private void registerEventClass(Class<? extends Event> eventClass) {
        if (allEvents.contains(eventClass)) {
            return;
        }

        try {
            Method getHandlerList = eventClass.getMethod("getHandlerList");
            if (!HandlerList.class.isAssignableFrom(getHandlerList.getReturnType())) {
                return;
            }
        } catch (NoSuchMethodException _) {
            return;
        }

        allEvents.add(eventClass);
        bySimpleName.putIfAbsent(eventClass.getSimpleName().toLowerCase(Locale.ROOT), eventClass);
    }
}
