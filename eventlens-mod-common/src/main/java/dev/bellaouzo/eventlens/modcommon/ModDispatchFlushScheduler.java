package dev.bellaouzo.eventlens.modcommon;

@FunctionalInterface
public interface ModDispatchFlushScheduler {

    void afterCurrentDispatch(Runnable task);
}
