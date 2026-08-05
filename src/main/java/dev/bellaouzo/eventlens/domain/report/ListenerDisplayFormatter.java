package dev.bellaouzo.eventlens.domain.report;

public final class ListenerDisplayFormatter {

    private static final String EVENT_LENS_CHECKPOINT = "EventLens trace checkpoint";

    private ListenerDisplayFormatter() {}

    public static String format(String pluginName, String listenerClassName, String methodName) {
        if (isEventLensCheckpoint(pluginName, listenerClassName)) {
            return EVENT_LENS_CHECKPOINT;
        }
        return simpleName(listenerClassName) + "#" + sanitizeMethodName(methodName);
    }

    private static boolean isEventLensCheckpoint(String pluginName, String listenerClassName) {
        return "EventLens".equalsIgnoreCase(pluginName) && listenerClassName.contains("PaperTraceHookManager");
    }

    private static String sanitizeMethodName(String methodName) {
        if (methodName.contains("Lambda") || methodName.contains("@") || methodName.equals("<lambda>")) {
            return "handler";
        }
        return methodName;
    }

    private static String simpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
}
