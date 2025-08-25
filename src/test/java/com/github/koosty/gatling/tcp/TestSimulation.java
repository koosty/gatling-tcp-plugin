package com.github.koosty.gatling.tcp;

import com.github.koosty.gatling.tcp.javaapi.TcpProtocolBuilder;
import com.github.koosty.gatling.tcp.javaapi.TcpValidators;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static com.github.koosty.gatling.tcp.javaapi.TcpDsl.tcp;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;

/**
 * A simple Gatling simulation that sends a TCP message to a test server
 * and checks the response using a custom validator.
 */
public class TestSimulation extends Simulation {
    TcpTestServer testServer = new TcpTestServer(2222);

    @Override
    public void after() {
        testServer.stop();
    }

    TcpProtocolBuilder tcpConfig = tcp()
            .host("localhost")
            .port(2222);

    ScenarioBuilder scn = scenario("TEST")
            .exec(
                    tcp("SEND DATA", "DATA".getBytes())
                            .withLengthHeader()
                            .withCheck(TcpValidators.notEmpty())
            );
    {
        setUp(
                scn.injectOpen(rampUsers(1).during(5))
        ).protocols(tcpConfig);
    }
}
