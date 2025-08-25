package com.github.koosty.gatling.tcp.javaapi;

import com.github.koosty.gatling.tcp.TcpProtocol;
import org.junit.jupiter.api.Test;

import static com.github.koosty.gatling.tcp.javaapi.TcpDsl.tcp;
import static org.junit.jupiter.api.Assertions.*;

class TcpProtocolBuilderTest {

    @Test
    void buildsProtocolWithHostAndPort() {
        com.github.koosty.gatling.tcp.TcpProtocolBuilder scalaBuilder = com.github.koosty.gatling.tcp.TcpProtocolBuilder.tcp();
        TcpProtocol tcpProtocol = new TcpProtocolBuilder(scalaBuilder)
                .host("localhost")
                .port(8080)
                .connectTimeout(1)
                .keepAlive(true)
                .reuseConnections(true)
                .readTimeout(1).protocol();
        assertEquals("localhost", tcpProtocol.host());
        assertEquals(8080, tcpProtocol.port());
        assertEquals(1, tcpProtocol.connectTimeout());
        assertTrue(tcpProtocol.keepAlive());
        assertTrue(tcpProtocol.reuseConnections());
        assertEquals(1, tcpProtocol.readTimeout());
    }

    @Test
    void handlesNullHostGracefully() {
        TcpProtocolBuilder tcpProtocolBuilder = tcp();
        assertThrows(NullPointerException.class, () -> tcpProtocolBuilder.host(null));
    }

    @Test
    void handlesNegativePortGracefully() {
        TcpProtocolBuilder tcpProtocolBuilder = tcp();
        assertThrows(IllegalArgumentException.class, () -> tcpProtocolBuilder.port(-1));
    }
}