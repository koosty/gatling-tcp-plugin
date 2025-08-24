package com.github.koosty.gatling.tcp.javaapi;

import io.gatling.javaapi.core.ActionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Builder class for creating TCP requests in Gatling simulations.
 * This class provides a fluent API for configuring TCP requests, including
 * setting the request name, message payload, length header options, and validators.
 */
public class TcpRequestBuilder implements ActionBuilder {
    private final String requestName;
    private byte[] message;
    private boolean addLengthHeader = false;
    private LengthHeaderType lengthHeaderType;
    private final List<Function<byte[], Boolean>> validators = new ArrayList<>();
    private boolean reuseConnection = false;
    private String connectionKey = "default";
    /**
     * Constructs a TcpRequestBuilder with a specified request name.
     *
     * @param requestName The name of the TCP request, used for identification in reports.
     */
    public TcpRequestBuilder(String requestName) {
        this.requestName = requestName;
    }

    /**
     * Constructs a TcpRequestBuilder with a specified message payload.
     *
     * @param message The byte array representing the TCP message to be sent.
     */
    public TcpRequestBuilder withMessage(byte[] message) {
        this.message = message;
        return this;
    }

    /**
     * Adds a validator function to check the response.
     *
     * @param validator A function that takes a byte array (response) and returns a boolean indicating validation success.
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestBuilder withCheck(Function<byte[], Boolean> validator) {
        this.validators.add(validator);
        return this;
    }

    /**
     * Enables automatic addition of a length header to the message.
     * By default, the length header is set to 2-byte big-endian format.
     *
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestBuilder withLengthHeader() {
        this.addLengthHeader = true;
        this.lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN; // Default to 2-byte big endian
        return this;
    }

    /**
     * Enables automatic addition of a length header to the message with a specified format.
     *
     * @param headerType The format of the length header (e.g., 2-byte or 4-byte, big-endian or little-endian).
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestBuilder withLengthHeader(LengthHeaderType headerType) {
        this.addLengthHeader = true;
        this.lengthHeaderType = headerType;
        return this;
    }

    /**
     * Enables connection reuse for this TCP request.
     * When enabled, the same TCP connection will be reused for multiple requests.
     *
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public  TcpRequestBuilder withReuseConnection() {
        this.reuseConnection = true;
        return this;
    }

    /**
     * Sets whether to reuse the TCP connection for this request.
     *
     * @param reuseConnection A boolean indicating whether to reuse the connection.
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public  TcpRequestBuilder withReuseConnection(boolean reuseConnection) {
        this.reuseConnection = reuseConnection;
        return this;
    }

    /**
     * Sets a custom connection key to identify the TCP connection.
     * This is useful when managing multiple connections in a simulation.
     *
     * @param connectionKey A string representing the connection key.
     * @return This TcpRequestBuilder instance for method chaining.
     */
    public TcpRequestBuilder withConnectionKey(String connectionKey) {
        this.connectionKey = connectionKey;
        return this;
    }

    /**
     * Converts this Java-based TCP request builder into a Scala-based action builder.
     *
     * @return A Scala-compatible ActionBuilder instance configured with the current settings.
     */
    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return new com.github.koosty.gatling.tcp.TcpRequestActionBuilder(
                requestName,
                message,
                addLengthHeader,
                lengthHeaderType,
                validators,
                reuseConnection,
                connectionKey
        );
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
