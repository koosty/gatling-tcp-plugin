package com.github.koosty.gatling.tcp.javaapi;

import com.github.koosty.gatling.tcp.TcpProtocol;
import io.gatling.core.protocol.Protocol;
import io.gatling.javaapi.core.ProtocolBuilder;

/**
 * TcpProtocolBuilder is a Java API wrapper for the underlying Scala TcpProtocolBuilder.
 * <p>
 * This builder allows configuration of TCP protocol settings for Gatling simulations,
 * including host, port, timeouts, keep-alive, and connection reuse options.
 * Each method returns a new instance with the updated configuration.
 * </p>
 */
public class TcpProtocolBuilder implements ProtocolBuilder {

    /**
     * The wrapped Scala TcpProtocolBuilder instance.
     */
    private final com.github.koosty.gatling.tcp.TcpProtocolBuilder wrapped;

    /**
     * Constructs a TcpProtocolBuilder wrapping the given Scala builder.
     *
     * @param wrapped the underlying Scala TcpProtocolBuilder
     */
    public TcpProtocolBuilder(com.github.koosty.gatling.tcp.TcpProtocolBuilder wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Sets the TCP host.
     * @param host the hostname or IP address
     * @return a new TcpProtocolBuilder with the host set
     */
    public TcpProtocolBuilder host(String host) {
        return new TcpProtocolBuilder(wrapped.host(host));
    }

    /**
     * Sets the TCP port.
     * @param port the port number
     * @return a new TcpProtocolBuilder with the port set
     */
    public TcpProtocolBuilder port(int port) {
        return new TcpProtocolBuilder(wrapped.port(port));
    }
    /**
     * Sets the TCP connection timeout in milliseconds.
     * @param connectTimeout timeout in ms
     * @return a new TcpProtocolBuilder with the connection timeout set
     */
    public TcpProtocolBuilder connectTimeout(int connectTimeout) {
        return new TcpProtocolBuilder(wrapped.connectTimeout(connectTimeout));
    }
    /**
     * Sets the TCP read timeout in milliseconds.
     * @param readTimeout timeout in ms
     * @return a new TcpProtocolBuilder with the read timeout set
     */
    public TcpProtocolBuilder readTimeout(int readTimeout) {
        return new TcpProtocolBuilder(wrapped.readTimeout(readTimeout));
    }
    /**
     * Enables or disables TCP keep-alive.
     * @param keepAlive true to enable, false to disable
     * @return a new TcpProtocolBuilder with keep-alive set
     */
    public TcpProtocolBuilder keepAlive(boolean keepAlive) {
        return new TcpProtocolBuilder(wrapped.keepAlive(keepAlive));
    }
    /**
     * Enables or disables connection reuse.
     * @param reuseConnections true to enable, false to disable
     * @return a new TcpProtocolBuilder with connection reuse set
     */
    public TcpProtocolBuilder reuseConnections(boolean reuseConnections) {
        return new TcpProtocolBuilder(wrapped.reuseConnections(reuseConnections));
    }

    /**
     * Builds and returns the configured TCP {@link Protocol} instance for Gatling.
     * @return the configured Protocol
     */
    @Override
    public TcpProtocol protocol() {
        return wrapped.protocol();
    }
}
