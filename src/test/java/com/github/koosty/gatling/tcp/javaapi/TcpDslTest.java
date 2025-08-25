package com.github.koosty.gatling.tcp.javaapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TcpDslTest {

    @Test
    @DisplayName("tcp(String) throws NullPointerException for null name")
    void tcpStringThrowsForNullName() {
        assertThrows(NullPointerException.class, () -> TcpDsl.tcp(null, null));
    }

    @Test
    @DisplayName("withLengthHeader(byte[]) prepends 2-byte big-endian header")
    void withLengthHeaderPrependsTwoByteBigEndianHeader() {
        byte[] message = {0x01, 0x02, 0x03};
        byte[] result = TcpDsl.withLengthHeader(message);
        assertEquals(5, result.length);
        assertEquals(0x00, result[0]); // length high byte
        assertEquals(0x03, result[1]); // length low byte
        assertEquals(0x01, result[2]);
        assertEquals(0x02, result[3]);
        assertEquals(0x03, result[4]);
    }

    @Test
    @DisplayName("withLengthHeader(byte[], headerType) prepends correct header for 4-byte big-endian")
    void withLengthHeaderPrependsFourByteBigEndianHeader() {
        byte[] message = {0x01, 0x02};
        byte[] result = TcpDsl.withLengthHeader(message, TcpRequestActionBuilder.LengthHeaderType.FOUR_BYTE_BIG_ENDIAN);
        assertEquals(6, result.length);
        assertEquals(0x00, result[0]);
        assertEquals(0x00, result[1]);
        assertEquals(0x00, result[2]);
        assertEquals(0x02, result[3]);
        assertEquals(0x01, result[4]);
        assertEquals(0x02, result[5]);
    }

    @Test
    @DisplayName("withLengthHeader(byte[], headerType) prepends correct header for 2-byte little-endian")
    void withLengthHeaderPrependsTwoByteLittleEndianHeader() {
        byte[] message = {0x01};
        byte[] result = TcpDsl.withLengthHeader(message, TcpRequestActionBuilder.LengthHeaderType.TWO_BYTE_LITTLE_ENDIAN);
        assertEquals(3, result.length);
        assertEquals(0x01, result[0]); // length low byte
        assertEquals(0x00, result[1]); // length high byte
        assertEquals(0x01, result[2]);
    }

    @Test
    @DisplayName("withLengthHeader(byte[], headerType) prepends correct header for 4-byte little-endian")
    void withLengthHeaderPrependsFourByteLittleEndianHeader() {
        byte[] message = {0x01, 0x02, 0x03};
        byte[] result = TcpDsl.withLengthHeader(message, TcpRequestActionBuilder.LengthHeaderType.FOUR_BYTE_LITTLE_ENDIAN);
        assertEquals(7, result.length);
        assertEquals(0x03, result[0]); // length low byte
        assertEquals(0x00, result[1]);
        assertEquals(0x00, result[2]);
        assertEquals(0x00, result[3]); // length high byte
        assertEquals(0x01, result[4]);
        assertEquals(0x02, result[5]);
        assertEquals(0x03, result[6]);
    }

    @Test
    @DisplayName("withLengthHeader(byte[]) handles empty message")
    void withLengthHeaderHandlesEmptyMessage() {
        byte[] message = {};
        byte[] result = TcpDsl.withLengthHeader(message);
        assertEquals(2, result.length);
        assertEquals(0x00, result[0]);
        assertEquals(0x00, result[1]);
    }
}

