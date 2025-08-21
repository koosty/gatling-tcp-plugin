package com.github.koosty.gatling.tcp.javaapi;

import java.util.function.Function;

/**
 * Utility class providing common TCP response validation functions.
 * These functions can be used to validate byte array responses or string responses
 * in TCP-based Gatling simulations.
 */
public class TcpValidators {

    private TcpValidators() {
    }

    /**
     * Validator to check if the response is not empty.
     *
     * @return A function that returns {@code true} if the response is not empty, {@code false} otherwise.
     */
    public static Function<byte[], Boolean> notEmpty() {
        return response -> response.length > 0;
    }

    /**
     * Validator to check if the response has a minimum length.
     *
     * @param minLength The minimum length the response should have.
     * @return A function that returns {@code true} if the response length is greater than or equal to {@code minLength}, {@code false} otherwise.
     */
    public static Function<byte[], Boolean> minLength(int minLength) {
        return response -> response.length >= minLength;
    }

    /**
     * Validator to check if the response does not exceed a maximum length.
     *
     * @param maxLength The maximum length the response can have.
     * @return A function that returns {@code true} if the response length is less than or equal to {@code maxLength}, {@code false} otherwise.
     */
    public static Function<byte[], Boolean> maxLength(int maxLength) {
        return response -> response.length <= maxLength;
    }

    /**
     * Validator to check if the response contains a specific sequence of bytes.
     *
     * @param expectedBytes The byte sequence to search for in the response.
     * @return A function that returns {@code true} if the response contains the {@code expectedBytes}, {@code false} otherwise.
     */
    public static Function<byte[], Boolean> containsBytes(byte[] expectedBytes) {
        return response -> {
            if (response.length < expectedBytes.length) return false;
            for (int i = 0; i <= response.length - expectedBytes.length; i++) {
                boolean found = true;
                for (int j = 0; j < expectedBytes.length; j++) {
                    if (response[i + j] != expectedBytes[j]) {
                        found = false;
                        break;
                    }
                }
                if (found) return true;
            }
            return false;
        };
    }

    /**
     * Validator to check if the response contains a specific string.
     *
     * @param expected The string to search for in the response.
     * @return A function that returns {@code true} if the response contains the {@code expected} string, {@code false} otherwise.
     */
    public static Function<String, Boolean> containsString(String expected) {
        return response -> response.contains(expected);
    }
}
