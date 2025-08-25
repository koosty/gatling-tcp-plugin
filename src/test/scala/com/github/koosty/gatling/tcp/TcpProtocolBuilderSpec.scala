package com.github.koosty.gatling.tcp

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

class TcpProtocolBuilderSpec extends AnyFlatSpec with Matchers with MockitoSugar {
  it should "build protocol with default settings" in {
    val builder = TcpProtocolBuilder.tcp()
    val protocol = builder.protocol()

    protocol.host shouldBe "localhost"
    protocol.port shouldBe 2222
    protocol.connectTimeout shouldBe 50000
    protocol.readTimeout shouldBe 10000
    protocol.keepAlive shouldBe true
    protocol.reuseConnections shouldBe true
  }

  it should "override default host and port" in {
    val builder = TcpProtocolBuilder.tcp()
      .host("example.com")
      .port(8080)
    val protocol = builder.protocol()

    protocol.host shouldBe "example.com"
    protocol.port shouldBe 8080
  }

  it should "override connection and read timeouts" in {
    val builder = TcpProtocolBuilder.tcp()
      .connectTimeout(3000)
      .readTimeout(6000)
    val protocol = builder.protocol()

    protocol.connectTimeout shouldBe 3000
    protocol.readTimeout shouldBe 6000
  }

  it should "disable keep-alive and reuse connections" in {
    val builder = TcpProtocolBuilder.tcp()
      .keepAlive(false)
      .reuseConnections(false)
    val protocol = builder.protocol()

    protocol.keepAlive shouldBe false
    protocol.reuseConnections shouldBe false
  }

  it should "throw exception for invalid port number" in {
    an[IllegalArgumentException] should be thrownBy TcpProtocolBuilder.tcp().port(-1)
  }

  it should "throw exception for null host" in {
    an[NullPointerException] should be thrownBy TcpProtocolBuilder.tcp().host(null)
  }
}
