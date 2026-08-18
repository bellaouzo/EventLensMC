package dev.bellaouzo.eventlens.clientagent;

import dev.bellaouzo.eventlens.observability.FabricListenerTimingWrapper;
import java.lang.reflect.Field;
import net.bytebuddy.asm.Advice;

public final class FabricListenerRegisterAdvice {

    private FabricListenerRegisterAdvice() {}

    @Advice.OnMethodEnter(suppress = Throwable.class)
    static void wrapListener(@Advice.This Object event, @Advice.AllArguments(readOnly = false) Object[] args) {
        if (args == null || args.length == 0) {
            return;
        }
        Object listener = args[args.length - 1];
        if (listener == null) {
            return;
        }
        Class<?> listenerType = handlerType(event);
        if (listenerType == null) {
            return;
        }
        String phaseId = args.length == 1 ? "minecraft:default" : String.valueOf(args[0]);
        args[args.length - 1] = FabricListenerTimingWrapper.wrap(listener, listenerType, phaseId);
    }

    private static Class<?> handlerType(Object event) {
        try {
            Field handlers = event.getClass().getDeclaredField("handlers");
            handlers.setAccessible(true);
            Object[] array = (Object[]) handlers.get(event);
            return array == null ? null : array.getClass().getComponentType();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
