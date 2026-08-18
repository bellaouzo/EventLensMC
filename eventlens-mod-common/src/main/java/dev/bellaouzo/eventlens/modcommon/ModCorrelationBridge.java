package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.correlation.CorrelationChannelCodec;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.modcommon.port.ModCorrelationChannelPort;
import dev.bellaouzo.eventlens.trace.DispatchCaptureListener;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;

public final class ModCorrelationBridge implements DispatchCaptureListener {

    private final TraceSessionManager sessionManager;
    private final ModCorrelationChannelPort channel;

    public ModCorrelationBridge(TraceSessionManager sessionManager, ModCorrelationChannelPort channel) {
        this.sessionManager = sessionManager;
        this.channel = channel == null ? ModCorrelationChannelPort.noop() : channel;
    }

    @Override
    public void onDispatchCaptured(String sessionId, TraceDispatchRecord dispatch) {
        dispatch.correlation()
                .correlationKey()
                .ifPresent(key -> channel.sendHello(sessionId, dispatch.sequence(), key));
    }

    public void receiveReply(byte[] payload) {
        CorrelationChannelCodec.parseReply(payload).ifPresent(this::stampClient);
    }

    private void stampClient(CorrelationChannelCodec.Reply reply) {
        sessionManager
                .getSessionDetail(reply.clientSessionId())
                .flatMap(detail -> detail.records().stream()
                        .filter(dispatch -> dispatch.sequence() == reply.clientSequence())
                        .findFirst())
                .flatMap(dispatch -> dispatch.correlation().correlationKey())
                .ifPresent(key -> sessionManager.stampPeer(
                        reply.clientSessionId(), key, reply.serverSessionId(), reply.serverSequence()));
    }
}
