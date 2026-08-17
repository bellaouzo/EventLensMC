package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

public final class PaperListenerRegistry implements ListenerRegistryPort {

    private final EventClassIndex eventClassIndex;

    public PaperListenerRegistry(ClassLoader classLoader) {
        this.eventClassIndex = new EventClassIndex(classLoader);
    }

    @Override
    public EventSearchResult searchEvents(String query) {
        List<Class<? extends Event>> matches = eventClassIndex.findMatches(query);
        if (matches.isEmpty()) {
            return EventSearchResult.notFound();
        }

        if (matches.size() == 1) {
            return EventSearchResult.found(matches.getFirst().getName());
        }

        List<String> candidates = matches.stream().map(Class::getName).toList();
        return EventSearchResult.ambiguous(candidates);
    }

    @Override
    public List<ListenerRegistration> getListeners(String eventClassName) {
        Class<? extends Event> eventClass = eventClassIndex.resolveClassName(eventClassName);
        HandlerList handlerList = getHandlerList(eventClass);
        RegisteredListener[] registeredListeners = handlerList.getRegisteredListeners();

        List<ListenerRegistration> listeners = new ArrayList<>(registeredListeners.length);
        for (int index = 0; index < registeredListeners.length; index++) {
            RegisteredListener registeredListener = registeredListeners[index];
            String methodName = ListenerMetadataExtractor.resolveMethodName(registeredListener, eventClass);

            listeners.add(new ListenerRegistration(
                    index + 1,
                    registeredListener.getPlugin().getName(),
                    registeredListener.getListener().getClass().getName(),
                    methodName,
                    registeredListener.getPriority().name(),
                    registeredListener.isIgnoringCancelled()));
        }

        return List.copyOf(listeners);
    }

    @Override
    public List<String> listKnownEventSimpleNames() {
        return eventClassIndex.listSimpleNames();
    }

    @Override
    public List<String> listKnownEventClassNames() {
        return eventClassIndex.listClassNames();
    }

    private static HandlerList getHandlerList(Class<? extends Event> eventClass) {
        try {
            Method getHandlerList = eventClass.getMethod("getHandlerList");
            return (HandlerList) getHandlerList.invoke(null);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Event class does not expose getHandlerList(): " + eventClass.getName(), ex);
        }
    }
}
