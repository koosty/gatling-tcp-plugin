package com.github.koosty.gatling.tcp.javaapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

class TcpValidatorsTest {

    @Test
    @DisplayName("notEmpty returns true for non-empty array")
    void notEmptyReturnsTrueForNonEmptyArray() {
        Function<byte[], Boolean> validator = TcpValidators.notEmpty();
        assertTrue(validator.apply(new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("notEmpty returns false for empty array")
    void notEmptyReturnsFalseForEmptyArray() {
        Function<byte[], Boolean> validator = TcpValidators.notEmpty();
        assertFalse(validator.apply(new byte[]{}));
    }

    @Test
    @DisplayName("minLength returns true when array length equals minLength")
    void minLengthReturnsTrueWhenArrayLengthEqualsMinLength() {
        Function<byte[], Boolean> validator = TcpValidators.minLength(3);
        assertTrue(validator.apply(new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("minLength returns true when array length greater than minLength")
    void minLengthReturnsTrueWhenArrayLengthGreaterThanMinLength() {
        Function<byte[], Boolean> validator = TcpValidators.minLength(2);
        assertTrue(validator.apply(new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("minLength returns false when array length less than minLength")
    void minLengthReturnsFalseWhenArrayLengthLessThanMinLength() {
        Function<byte[], Boolean> validator = TcpValidators.minLength(4);
        assertFalse(validator.apply(new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("minLength returns true for minLength zero and empty array")
    void minLengthReturnsTrueForMinLengthZeroAndEmptyArray() {
        Function<byte[], Boolean> validator = TcpValidators.minLength(0);
        assertTrue(validator.apply(new byte[]{}));
    }
}

