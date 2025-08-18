package com.github.koosty.gatling.tcp.javaapi;

public class TcpDsl {
    // Prevent instantiation
    private TcpDsl() {}

    public static TcpProtocolBuilder tcp() {
        return new TcpProtocolBuilder(
                com.github.koosty.gatling.tcp.TcpProtocolBuilder.tcp()
        );
    }

    public static TcpRequestBuilderInit tcp(String requestName) {
        return new TcpRequestBuilderInit(requestName);
    }

    public static class TcpRequestBuilderInit {
        private final String requestName;

        TcpRequestBuilderInit(String requestName) {
            this.requestName = requestName;
        }

        public TcpRequestBuilder send(byte[] message) {
            return new TcpRequestBuilder(requestName, message);
        }
    }
}
