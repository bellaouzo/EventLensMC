package dev.bellaouzo.eventlens.domain.correlation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CorrelationChannelCodecTest {

    @Test
    void helloRoundTrip() {
        byte[] payload = CorrelationChannelCodec.hello("abcd1234", 3L, "USE_BLOCK|p|w|1,2|10");
        var hello = CorrelationChannelCodec.parseHello(payload).orElseThrow();
        assertEquals("abcd1234", hello.clientSessionId());
        assertEquals(3L, hello.sequence());
        assertEquals("USE_BLOCK|p|w|1,2|10", hello.correlationKey());
    }

    @Test
    void replyRoundTrip() {
        byte[] payload = CorrelationChannelCodec.reply("server01", 8L, "client01", 2L);
        var reply = CorrelationChannelCodec.parseReply(payload).orElseThrow();
        assertEquals("server01", reply.serverSessionId());
        assertEquals(8L, reply.serverSequence());
        assertEquals("client01", reply.clientSessionId());
        assertEquals(2L, reply.clientSequence());
    }

    @Test
    void invalidPayloadIsIgnored() {
        assertTrue(CorrelationChannelCodec.parseHello("NOPE".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .isEmpty());
        assertTrue(CorrelationChannelCodec.parseReply(CorrelationChannelCodec.hello("a", 1L, "k"))
                .isEmpty());
    }
}
