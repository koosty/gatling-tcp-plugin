package com.github.koosty.gatling.tcp.javaapi;

import io.gatling.core.protocol.Protocol;
import io.gatling.javaapi.core.ProtocolBuilder;

public class TcpProtocolBuilder implements ProtocolBuilder {

    private final com.github.koosty.gatling.tcp.TcpProtocolBuilder wrapped;

    public TcpProtocolBuilder(com.github.koosty.gatling.tcp.TcpProtocolBuilder wrapped) {
        this.wrapped = wrapped;
    }

    public TcpProtocolBuilder host(String host) {
        return new TcpProtocolBuilder(wrapped.host(host));
    }

    public TcpProtocolBuilder port(int port) {
        return new TcpProtocolBuilder(wrapped.port(port));
    }
    public TcpProtocolBuilder connectTimeout(int connectTimeout) {
        return new TcpProtocolBuilder(wrapped.connectTimeout(connectTimeout));
    }
    public TcpProtocolBuilder readTimeout(int readTimeout) {
        return new TcpProtocolBuilder(wrapped.readTimeout(readTimeout));
    }
    public TcpProtocolBuilder keepAlive(boolean keepAlive) {
        return new TcpProtocolBuilder(wrapped.keepAlive(keepAlive));
    }
    public TcpProtocolBuilder reuseConnections(boolean reuseConnections) {
        return new TcpProtocolBuilder(wrapped.reuseConnections(reuseConnections));
    }

    @Override
    public Protocol protocol() {
        return wrapped.protocol();
    }
}
