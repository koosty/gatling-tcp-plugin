package com.github.koosty.gatling.tcp.javaapi;

import java.util.Objects;

/**
 * TcpDsl provides a Java DSL for building TCP protocol and request objects
 * for Gatling TCP plugin simulations.
 * <p>
 * This class contains static factory methods to create protocol and request builders,
 * and utility methods for message formatting.
 * </p>
 */
public class TcpDsl {
    /**
     * Prevents instantiation of TcpDsl utility class.
     */
    private TcpDsl() {}

    /**
     * Creates a new {@link TcpProtocolBuilder} for configuring TCP protocol settings.
     *
     * @return a new TcpProtocolBuilder instance
     */
    public static TcpProtocolBuilder tcp() {
        return new TcpProtocolBuilder(
                com.github.koosty.gatling.tcp.TcpProtocolBuilder.tcp()
        );
    }

    /**
     * Initializes a TCP request builder with the given request name.
     *
     * @param requestName the name of the TCP request
     * @return a TcpRequestBuilderInit instance for further configuration
     */
    public static TcpRequestBuilder tcp(String requestName) {
        Objects.requireNonNull(requestName, "Request name must not be null");
        return new TcpRequestBuilder(requestName);
    }

    /**
     * Prepends a length header to the given message using the default
     * {@link TcpRequestBuilder.LengthHeaderType#TWO_BYTE_BIG_ENDIAN} format.
     *
     * @param message the message to format
     * @return the message with a length header prepended
     */
    public static byte[] withLengthHeader(byte[] message) {
        return withLengthHeader(message, TcpRequestBuilder.LengthHeaderType.TWO_BYTE_BIG_ENDIAN);
    }

    /**
     * Prepends a length header to the given message using the specified
     * length header type.
     *
     * @param message the message to format
     * @param headerType the type of length header to use
     * @return the message with a length header prepended
     */
    public static byte[] withLengthHeader(byte[] message, TcpRequestBuilder.LengthHeaderType headerType) {
        int headerSize = (headerType == TcpRequestBuilder.LengthHeaderType.TWO_BYTE_BIG_ENDIAN ||
                headerType == TcpRequestBuilder.LengthHeaderType.TWO_BYTE_LITTLE_ENDIAN) ? 2 : 4;

        byte[] result = new byte[message.length + headerSize];

        byte[] lengthHeader = createLengthHeaderBytes(message.length, headerType);
        System.arraycopy(lengthHeader, 0, result, 0, headerSize);
        System.arraycopy(message, 0, result, headerSize, message.length);

        return result;
    }

    private static byte[] createLengthHeaderBytes(int length, TcpRequestBuilder.LengthHeaderType headerType) {
        switch (headerType) {
            case TWO_BYTE_BIG_ENDIAN:
                return new byte[]{
                        (byte) ((length >> 8) & 0xFF),
                        (byte) (length & 0xFF)
                };
            case TWO_BYTE_LITTLE_ENDIAN:
                return new byte[]{
                        (byte) (length & 0xFF),
                        (byte) ((length >> 8) & 0xFF)
                };
            case FOUR_BYTE_BIG_ENDIAN:
                return new byte[]{
                        (byte) ((length >> 24) & 0xFF),
                        (byte) ((length >> 16) & 0xFF),
                        (byte) ((length >> 8) & 0xFF),
                        (byte) (length & 0xFF)
                };
            case FOUR_BYTE_LITTLE_ENDIAN:
                return new byte[]{
                        (byte) (length & 0xFF),
                        (byte) ((length >> 8) & 0xFF),
                        (byte) ((length >> 16) & 0xFF),
                        (byte) ((length >> 24) & 0xFF)
                };
            default:
                throw new IllegalArgumentException("Unsupported header type: " + headerType);
        }
    }
}
