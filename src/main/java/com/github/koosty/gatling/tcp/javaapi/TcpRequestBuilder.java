package com.github.koosty.gatling.tcp.javaapi;

import io.gatling.javaapi.core.ActionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TcpRequestBuilder implements ActionBuilder {
    private final String requestName;
    private final byte[] message;
    private boolean expectResponse = true;
    private final List<Function<byte[], Boolean>> validators = new ArrayList<>();

    public TcpRequestBuilder(String requestName, byte[] message) {
        this.requestName = requestName;
        this.message = message;
    }

    public TcpRequestBuilder noResponse() {
        this.expectResponse = false;
        return this;
    }

    public TcpRequestBuilder check(Function<byte[], Boolean> validator) {
        this.validators.add(validator);
        return this;
    }
    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return new com.github.koosty.gatling.tcp.TcpRequestActionBuilder(
                requestName,
                message,
                expectResponse,
                validators
        );
    }
}
