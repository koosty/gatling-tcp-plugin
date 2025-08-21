package com.github.koosty.gatling.tcp

import io.gatling.core.protocol.Protocol

/** Builder class for creating TCP protocol configurations in Gatling.
 *
 * This class provides a fluent builder interface for configuring TCP connection parameters
 * used in Gatling performance tests.
 */
class TcpProtocolBuilder {
  private var _host: String = "localhost"
  private var _port: Int = 2222
  private var _connectTimeout: Int = 50000
  private var _readTimeout: Int = 10000
  private var _keepAlive: Boolean = true
  private var _reuseConnections: Boolean = true

  /** Sets the target host for TCP connections.
   *
   * @param h The hostname or IP address to connect to
   * @return This builder instance for method chaining
   */
  def host(h: String): TcpProtocolBuilder = { _host = h; this }

  /** Sets the TCP port number to connect to.
   *
   * @param p The port number
   * @return This builder instance for method chaining
   */
  def port(p: Int): TcpProtocolBuilder = { _port = p; this }

  /** Sets the connection timeout value.
   *
   * @param timeout The timeout in milliseconds for establishing new connections
   * @return This builder instance for method chaining
   */
  def connectTimeout(timeout: Int): TcpProtocolBuilder = { _connectTimeout = timeout; this }

  /** Sets the read timeout value.
   *
   * @param timeout The timeout in milliseconds for reading data from the connection
   * @return This builder instance for method chaining
   */
  def readTimeout(timeout: Int): TcpProtocolBuilder = { _readTimeout = timeout; this }

  /** Enables or disables TCP keep-alive.
   *
   * @param enabled Whether to enable TCP keep-alive
   * @return This builder instance for method chaining
   */
  def keepAlive(enabled: Boolean): TcpProtocolBuilder = { _keepAlive = enabled; this }

  /** Controls whether connections should be reused across requests.
   *
   * @param enabled Whether to enable connection reuse
   * @return This builder instance for method chaining
   */
  def reuseConnections(enabled: Boolean): TcpProtocolBuilder = { _reuseConnections = enabled; this }

  /** Builds and returns the final TCP protocol configuration.
   *
   * @return A Protocol instance configured with the current builder settings
   */
  def protocol(): Protocol = {
    TcpProtocol(_host, _port, _connectTimeout, _readTimeout, _keepAlive)
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
