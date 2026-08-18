package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;

public final class ModHudFindings {

    private ModHudFindings() {}

    public static String statusBits(TraceDispatchRecord last) {
        String cancel = last.cancelledAtEnd() ? "cancelled" : "ok";
        return last.correlation().linked() ? cancel + "  ·  linked" : cancel;
    }

    public static String exportPeerLabel(boolean peerFound) {
        return peerFound ? "Peer found" : "No peer";
    }
}
