package com.github.koosty.gatling.tcp.javaapi;

import io.gatling.javaapi.core.ActionBuilder;

import java.util.function.Function;

/**
 * Builder class for creating TCP requests in Gatling simulations.
 * This class provides a fluent API for configuring TCP requests, including
 * setting the request name, message payload, length header options, and validators.
 */
public class TcpRequestActionBuilder implements ActionBuilder {
    private final com.github.koosty.gatling.tcp.TcpRequestActionBuilder wrapped;

    public TcpRequestActionBuilder(com.github.koosty.gatling.tcp.TcpRequestActionBuilder wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Adds a validator function to check the response.
     *
     * @param validator A function that takes a byte array (response) and returns a boolean indicating validation success.
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestActionBuilder withCheck(Function<byte[], Boolean> validator) {
        this.wrapped.validators().add(validator);
        return new TcpRequestActionBuilder(this.wrapped);
    }

    /**
     * Enables automatic addition of a length header to the message.
     * By default, the length header is set to 2-byte big-endian format.
     *
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestActionBuilder withLengthHeader() {
        return new TcpRequestActionBuilder(this.wrapped.addLengthHeader(true).lengthHeaderType(LengthHeaderType.TWO_BYTE_BIG_ENDIAN));
    }

    /**
     * Enables automatic addition of a length header to the message with a specified format.
     *
     * @param lengthHeaderType The format of the length header (e.g., 2-byte or 4-byte, big-endian or little-endian).
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestActionBuilder withLengthHeader(LengthHeaderType lengthHeaderType) {
        return new TcpRequestActionBuilder(this.wrapped.addLengthHeader(true).lengthHeaderType(lengthHeaderType));
    }

    /**
     * Enables connection reuse for this TCP request.
     * When enabled, the same TCP connection will be reused for multiple requests.
     *
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestActionBuilder withReuseConnection() {
        return new TcpRequestActionBuilder(this.wrapped.reuseConnection(true));
    }

    /**
     * Sets whether to reuse the TCP connection for this request.
     *
     * @param reuseConnection A boolean indicating whether to reuse the connection.
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestActionBuilder withReuseConnection(boolean reuseConnection) {
        return new TcpRequestActionBuilder(this.wrapped.reuseConnection(reuseConnection));
    }

    /**
     * Sets a custom connection key to identify the TCP connection.
     * This is useful when managing multiple connections in a simulation.
     *
     * @param connectionKey A string representing the connection key.
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestActionBuilder withConnectionKey(String connectionKey) {
        return new TcpRequestActionBuilder(this.wrapped.connectionKey(connectionKey));
    }

    /**
     * Converts this Java-based TCP request builder into a Scala-based action builder.
     *
     * @return A Scala-compatible ActionBuilder instance configured with the current settings.
     */
    @Override
    public com.github.koosty.gatling.tcp.TcpRequestActionBuilder  asScala() {
        return wrapped;
    }

    /**
     * Enum representing the possible formats for the length header.
     * The length header can be either 2 bytes or 4 bytes, and can use big-endian or little-endian encoding.
     */
    public enum LengthHeaderType {
        TWO_BYTE_BIG_ENDIAN,    // Default: 2 bytes, big endian
        TWO_BYTE_LITTLE_ENDIAN, // 2 bytes, little endian
        FOUR_BYTE_BIG_ENDIAN,   // 4 bytes, big endian
        FOUR_BYTE_LITTLE_ENDIAN // 4 bytes, little endian
    }
}
