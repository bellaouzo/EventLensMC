package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.modcommon.ModAgentInstrumentationAdapter;
import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModNoOpInstrumentationAdapter;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import net.minecraft.client.Minecraft;

final class ForgeClientInstrumentation {

    private static final String AGENT_LOADED = "dev.bellaouzo.eventlens.agent.loaded";

    private ForgeClientInstrumentation() {}

    static Result create(TraceSessionManager sessionManager) {
        if (!Boolean.getBoolean(AGENT_LOADED)) {
            return dispatchOnly(sessionManager);
        }
        try {
            ModAgentInstrumentationAdapter adapter = ModAgentInstrumentationAdapter.createAndRegister(
                    new ForgeListenerSnapshotBridge(), new ForgeModIdResolver());
            ModDispatchRecorder recorder =
                    new ModDispatchRecorder(sessionManager, adapter, ForgeClientInstrumentation::enqueue);
            return new Result(adapter, recorder);
        } catch (LinkageError ignored) {
            return dispatchOnly(sessionManager);
        }
    }

    private static Result dispatchOnly(TraceSessionManager sessionManager) {
        return new Result(new ModNoOpInstrumentationAdapter(), new ModDispatchRecorder(sessionManager));
    }

    private static void enqueue(Runnable task) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.execute(task);
        } else {
            task.run();
        }
    }

    record Result(InstrumentationPort port, ModDispatchRecorder recorder) {}
}
