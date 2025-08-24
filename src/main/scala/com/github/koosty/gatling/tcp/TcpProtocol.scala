package com.github.koosty.gatling.tcp

import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

/** A protocol implementation for TCP connections in Gatling performance tests.
 *
 * @param host The target host to connect to
 * @param port The TCP port number to connect to
 * @param connectTimeout The timeout in milliseconds for establishing a connection
 * @param readTimeout The timeout in milliseconds for reading data from the connection
 * @param keepAlive Whether to use TCP keep-alive
 */
case class TcpProtocol(host: String, port: Int, connectTimeout: Int, readTimeout: Int, keepAlive: Boolean) extends Protocol

/** Components for managing TCP protocol state during Gatling simulations.
 *
 * This class handles the lifecycle of TCP connections during performance tests.
 *
 * @param protocol The TCP protocol configuration to use
 */
case class TcpComponents(protocol: TcpProtocol) extends ProtocolComponents {
  /** Called when a virtual user starts their session.
   *
   * @return The potentially modified session
   */
  override def onStart: Session => Session = session => session

  /** Called when a virtual user ends their session.
   *
   * */
  override def onExit: Session => Unit = _ => ()
}

/** Companion object for TcpProtocol containing protocol configuration and factory methods.
 */
object TcpProtocol {
  /** Protocol key for registering the TCP protocol with Gatling.
   *
   * Provides default configuration and component initialization for the TCP protocol.
   */
  val TcpProtocolKey: ProtocolKey[TcpProtocol, TcpComponents] = new ProtocolKey[TcpProtocol, TcpComponents] {
    override def protocolClass: Class[Protocol] =
      classOf[TcpProtocol].asInstanceOf[Class[Protocol]]

    /** Creates a default protocol configuration.
     *
     * @param configuration The Gatling configuration
     * @return A TcpProtocol instance with default settings
     */
    override def defaultProtocolValue(configuration: GatlingConfiguration): TcpProtocol =
      TcpProtocol("localhost", 2222, 5000, 10000, keepAlive = true)

    /** Creates new protocol components for a simulation.
     *
     * @param coreComponents The core Gatling components
     * @return A function that creates TcpComponents from a protocol instance
     */
    override def newComponents(coreComponents: CoreComponents): TcpProtocol => TcpComponents =
      protocol => TcpComponents(protocol)
  }
}