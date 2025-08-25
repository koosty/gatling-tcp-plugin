package com.github.koosty.gatling.tcp.javaapi;

import com.github.koosty.gatling.tcp.TcpProtocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.github.koosty.gatling.tcp.javaapi.TcpDsl.tcp;
import static org.junit.jupiter.api.Assertions.*;

class TcpProtocolBuilderTest {

    @ParameterizedTest
    @CsvSource({
            "localhost, 2222, 50000, 10000, true, true",
            "example.com, 8080, 1000, 2000, false, false",
            "127.0.0.1, 80, 1, 1, false, true",
            "test.server, 65535, 30000, 5000, true, false",
            "localhost, 1, 0, 0, true, true"
    })
    void buildsProtocolWithHostAndPort(String host, int port, int connectTimeout, int readTimeout,
                                       boolean keepAlive, boolean reuseConnections) {
        com.github.koosty.gatling.tcp.TcpProtocolBuilder scalaBuilder = com.github.koosty.gatling.tcp.TcpProtocolBuilder.tcp();
        TcpProtocol tcpProtocol = new TcpProtocolBuilder(scalaBuilder)
                .host(host)
                .port(port)
                .connectTimeout(connectTimeout)
                .keepAlive(keepAlive)
                .reuseConnections(reuseConnections)
                .readTimeout(readTimeout).protocol();
        assertEquals(host, tcpProtocol.host());
        assertEquals(port, tcpProtocol.port());
        assertEquals(connectTimeout, tcpProtocol.connectTimeout());
        assertEquals(keepAlive, tcpProtocol.keepAlive());
        assertEquals(reuseConnections, tcpProtocol.reuseConnections());
        assertEquals(readTimeout, tcpProtocol.readTimeout());
    }

    @Test
    void handlesNullHostGracefully() {
        TcpProtocolBuilder tcpProtocolBuilder = tcp();
        assertThrows(NullPointerException.class, () -> tcpProtocolBuilder.host(null));
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "65536"
    })
    void handlesNegativePortGracefully() {
        TcpProtocolBuilder tcpProtocolBuilder = tcp();
        assertThrows(IllegalArgumentException.class, () -> tcpProtocolBuilder.port(-1));
    }
}