package com.github.koosty.gatling.tcp

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Session
import io.netty.channel.EventLoop
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

class TcpProtocolSpec extends AnyFlatSpec with Matchers with MockitoSugar{
  implicit val mockEventLoop: EventLoop = mock[EventLoop]
  "TcpProtocol" should "create with correct parameters" in {
    val protocol = TcpProtocol("test-host", 9090, 3000, 5000, keepAlive = true, reuseConnections = false)

    protocol.host shouldBe "test-host"
    protocol.port shouldBe 9090
    protocol.connectTimeout shouldBe 3000
    protocol.readTimeout shouldBe 5000
    protocol.keepAlive shouldBe true
    protocol.reuseConnections shouldBe false
  }

  "TcpComponents" should "handle session lifecycle correctly" in {
    val protocol = TcpProtocol("localhost", 8080, 5000, 10000, keepAlive = true, reuseConnections = true)
    val components = TcpComponents(protocol)
    val session = Session("tcp-test-scenario", userId = 1L, mockEventLoop)

    // Test onStart
    val startedSession = components.onStart(session)
    startedSession shouldBe session

    // Test onExit (should not throw)
    noException should be thrownBy components.onExit(session)
  }

  "TcpProtocolKey" should "provide correct default values" in {
    val config = GatlingConfiguration.loadForTest()
    val defaultProtocol = TcpProtocol.TcpProtocolKey.defaultProtocolValue(config)

    defaultProtocol.host shouldBe "localhost"
    defaultProtocol.port shouldBe 2222
    defaultProtocol.connectTimeout shouldBe 5000
    defaultProtocol.readTimeout shouldBe 10000
    defaultProtocol.keepAlive shouldBe true
    defaultProtocol.reuseConnections shouldBe true
  }
}
