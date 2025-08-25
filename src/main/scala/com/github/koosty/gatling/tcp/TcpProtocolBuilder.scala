package com.github.koosty.gatling.tcp

import io.gatling.internal.quicklens._

import java.util.Objects

/** Builder class for creating TCP protocol configurations in Gatling.
 *
 * This class provides a fluent builder interface for configuring TCP connection parameters
 * used in Gatling performance tests.
 */
case class TcpProtocolBuilder(
                               host: String = "localhost",
                               port: Int = 2222,
                               connectTimeout: Int = 50000,
                               readTimeout: Int = 10000,
                               keepAlive: Boolean = true,
                               reuseConnections: Boolean = true
                             ) {


  /** Sets the target host for TCP connections.
   *
   * @param host The hostname or IP address to connect to
   * @return This builder instance for method chaining
   */
  def host(host: String): TcpProtocolBuilder = {
    Objects.requireNonNull(host, "Host cannot be null")
    this.modify(_.host).setTo(host)
  }

  /** Sets the TCP port number to connect to.
   *
   * @param port The port number
   * @return This builder instance for method chaining
   */
  def port(port: Int): TcpProtocolBuilder = {
    if (port<1 || port>65535) {
      throw new IllegalArgumentException(s"Port number must be between 1 and 65535, got: $port")
    }

    this.modify(_.port).setTo(port)
  }

  /** Sets the connection timeout value.
   *
   * @param connectTimeout The timeout in milliseconds for establishing new connections
   * @return This builder instance for method chaining
   */
  def connectTimeout(connectTimeout: Int): TcpProtocolBuilder = this.modify(_.connectTimeout).setTo(connectTimeout)

  /** Sets the read timeout value.
   *
   * @param readTimeout The timeout in milliseconds for reading data from the connection
   * @return This builder instance for method chaining
   */
  def readTimeout(readTimeout: Int): TcpProtocolBuilder = this.modify(_.readTimeout).setTo(readTimeout)

  /** Enables or disables TCP keep-alive.
   *
   * @param keepAlive Whether to enable TCP keep-alive
   * @return This builder instance for method chaining
   */
  def keepAlive(keepAlive: Boolean): TcpProtocolBuilder = this.modify(_.keepAlive).setTo(keepAlive)

  /** Controls whether connections should be reused across requests.
   *
   * @param reuseConnections Whether to enable connection reuse
   * @return This builder instance for method chaining
   */
  def reuseConnections(reuseConnections: Boolean): TcpProtocolBuilder = this.modify(_.reuseConnections).setTo(reuseConnections)

  /** Builds and returns the final TCP protocol configuration.
   *
   * @return A Protocol instance configured with the current builder settings
   */
  def protocol(): TcpProtocol = {
    TcpProtocol(host, port, connectTimeout, readTimeout, keepAlive, reuseConnections)
  }
}

/** Companion object providing factory methods for creating TCP protocol builders.
 */
object TcpProtocolBuilder {
  /** Creates a new TCP protocol builder with default settings.
   *
   * @return A new TcpProtocolBuilder instance
   */
  def tcp(): TcpProtocolBuilder = new TcpProtocolBuilder()
}
