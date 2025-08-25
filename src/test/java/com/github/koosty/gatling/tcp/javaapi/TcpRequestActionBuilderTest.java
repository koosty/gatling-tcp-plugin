package com.github.koosty.gatling.tcp.javaapi;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TcpRequestActionBuilderTest {

    @Test
    void addsValidatorSuccessfully() {
        var wrapped = com.github.koosty.gatling.tcp.TcpRequestActionBuilder.request("test-request", new byte[]{0x01, 0x02});
        var builder = new TcpRequestActionBuilder(wrapped);

        Function<byte[], Boolean> validator = bytes -> bytes.length > 0;
        builder.withCheck(validator);
        com.github.koosty.gatling.tcp.TcpRequestActionBuilder scalaBuilder = builder.asScala();
        assertFalse(scalaBuilder.validators().isEmpty());
    }

    @Test
    void enablesLengthHeaderWithDefaultType() {
        var wrapped = com.github.koosty.gatling.tcp.TcpRequestActionBuilder.request("test-request", new byte[]{0x01, 0x02});

        var builder = new TcpRequestActionBuilder(wrapped).withLengthHeader();

        com.github.koosty.gatling.tcp.TcpRequestActionBuilder scalaBuilder = builder.asScala();
        assertTrue(scalaBuilder.addLengthHeader());
        assertEquals(TcpRequestActionBuilder.LengthHeaderType.TWO_BYTE_BIG_ENDIAN, scalaBuilder.lengthHeaderType());
    }

    @Test
    void enablesLengthHeaderWithCustomType() {
        var wrapped = com.github.koosty.gatling.tcp.TcpRequestActionBuilder.request("test-request", new byte[]{0x01, 0x02});

        var builder = new TcpRequestActionBuilder(wrapped)
                .withLengthHeader(TcpRequestActionBuilder.LengthHeaderType.FOUR_BYTE_LITTLE_ENDIAN);

        com.github.koosty.gatling.tcp.TcpRequestActionBuilder scalaBuilder = builder.asScala();
        assertTrue(scalaBuilder.addLengthHeader());
        assertEquals(TcpRequestActionBuilder.LengthHeaderType.FOUR_BYTE_LITTLE_ENDIAN, scalaBuilder.lengthHeaderType());
    }

    @Test
    void enablesConnectionReuse() {
        var wrapped = com.github.koosty.gatling.tcp.TcpRequestActionBuilder.request("test-request", new byte[]{0x01, 0x02});

        var builder = new TcpRequestActionBuilder(wrapped).withReuseConnection();

        com.github.koosty.gatling.tcp.TcpRequestActionBuilder scalaBuilder = builder.asScala();
        assertTrue(scalaBuilder.reuseConnection());
    }

    @Test
    void setsCustomConnectionKey() {
        var wrapped = com.github.koosty.gatling.tcp.TcpRequestActionBuilder.request("test-request", new byte[]{0x01, 0x02});

        var builder = new TcpRequestActionBuilder(wrapped).withConnectionKey("custom-key");

        com.github.koosty.gatling.tcp.TcpRequestActionBuilder scalaBuilder = builder.asScala();
        assertEquals("custom-key", scalaBuilder.connectionKey());
    }

    @Test
    void convertsToScalaActionBuilder() {
        var wrapped = com.github.koosty.gatling.tcp.TcpRequestActionBuilder.request("test-request", new byte[]{0x01, 0x02});
        var builder = new TcpRequestActionBuilder(wrapped);

        com.github.koosty.gatling.tcp.TcpRequestActionBuilder scalaBuilder = builder.asScala();
        assertEquals(wrapped, scalaBuilder);
    }
}