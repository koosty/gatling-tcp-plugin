package com.github.koosty.gatling.tcp

import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

final case class TcpProtocol(host: String, port: Int, connectTimeout: Int, readTimeout: Int, keepAlive: Boolean, reuseConnections: Boolean) extends Protocol

final case class TcpComponents(protocol: TcpProtocol) extends ProtocolComponents {
  // Called when a virtual user starts
  override def onStart: Session => Session = session => session
  override def onExit: Session => Unit = _ => ()
  // Extend with additional fields/resources as needed
}

object TcpProtocol {
  val TcpProtocolKey: ProtocolKey[TcpProtocol, TcpComponents] = new ProtocolKey[TcpProtocol, TcpComponents] {
    override def protocolClass: Class[Protocol] =
      classOf[TcpProtocol].asInstanceOf[Class[Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): TcpProtocol =
      TcpProtocol("localhost", 2222, 5000, 10000, keepAlive = true, reuseConnections = false)

    override def newComponents(coreComponents: CoreComponents): TcpProtocol => TcpComponents =
      protocol => TcpComponents(protocol)
  }
}