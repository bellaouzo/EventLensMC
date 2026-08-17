package dev.bellaouzo.eventlens.clientagent;

final class ClientAgentTargets {

    private ClientAgentTargets() {}

    static boolean matches(Class<?> type, String eventClassName) {
        try {
            type.getMethod("invoke", Class.forName(eventClassName, false, type.getClassLoader()));
            return true;
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }
}
