# Gatling TCP Plugin

A plugin for [Gatling](https://gatling.io/) to enable TCP protocol support in load tests.

## Features
- TCP protocol support for Gatling
- Custom simulation examples
- Easy configuration

## Installation
Add this plugin as a dependency to your Gatling project.
```xml
<dependency>
    <groupId>io.github.koosty</groupId>
    <artifactId>gatling-tcp-plugin</artifactId>
    <version>[VERSION]</version>
</dependency>
```
Search for the latest version on [Maven Central](https://search.maven.org/artifact/io.github.koosty/gatling-tcp-plugin).

## Usage
```java

public class TestSimulation extends Simulation {

    TcpProtocolBuilder tcpConfig = tcp()
            .host("localhost")
            .port(2222);

    ScenarioBuilder scn = scenario("TEST")
            .exec(
                    tcp("SEND DATA")
                            .withMessage("DATA".getBytes())
                            .withLengthHeader()
                            .withCheck(TcpValidators.notEmpty())
            );
    {
        setUp(
                scn.injectOpen(rampUsers(1).during(5))
        ).protocols(tcpConfig);
    }
}
```
See `src/test/java` for complete examples.

## Contributing
Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md).

## Code of Conduct
See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Security
See [SECURITY.md](SECURITY.md).

## License
This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).
