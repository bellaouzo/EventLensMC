package dev.bellaouzo.eventlens.paper;

import java.lang.reflect.Method;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

final class ListenerMetadataExtractor {

    private ListenerMetadataExtractor() {}

    static String resolveMethodName(RegisteredListener registeredListener, Class<? extends Event> eventClass) {
        Listener listener = registeredListener.getListener();
        String fromAnnotation = findAnnotatedMethod(listener.getClass(), eventClass);
        if (fromAnnotation != null) {
            return fromAnnotation;
        }

        String executorLabel = registeredListener.getExecutor().toString();
        if (executorLabel != null && !executorLabel.isBlank()) {
            return executorLabel;
        }

        return "unknown";
    }

    private static String findAnnotatedMethod(Class<?> listenerClass, Class<? extends Event> eventClass) {
        for (Method method : listenerClass.getMethods()) {
            if (isHandlerForEvent(method, eventClass)) {
                return method.getName();
            }
        }

        return null;
    }

    private static boolean isHandlerForEvent(Method method, Class<? extends Event> eventClass) {
        EventHandler handler = method.getAnnotation(EventHandler.class);
        if (handler == null || method.getParameterCount() != 1) {
            return false;
        }

        Class<?> parameterType = method.getParameterTypes()[0];
        return Event.class.isAssignableFrom(parameterType) && parameterType.isAssignableFrom(eventClass);
    }
}
