package com.github.koosty.gatling.tcp

import io.gatling.core.protocol.Protocol

class TcpProtocolBuilder {
  private var _host: String = "localhost"
  private var _port: Int = 2222
  private var _connectTimeout: Int = 5000
  private var _readTimeout: Int = 10000
  private var _keepAlive: Boolean = true
  private var _reuseConnections: Boolean = true

  def host(h: String): TcpProtocolBuilder = { _host = h; this }
  def port(p: Int): TcpProtocolBuilder = { _port = p; this }
  def connectTimeout(timeout: Int): TcpProtocolBuilder = { _connectTimeout = timeout; this }
  def readTimeout(timeout: Int): TcpProtocolBuilder = { _readTimeout = timeout; this }
  def keepAlive(enabled: Boolean): TcpProtocolBuilder = { _keepAlive = enabled; this }
  def reuseConnections(enabled: Boolean): TcpProtocolBuilder = { _reuseConnections = enabled; this }

  def protocol(): Protocol = {
    TcpProtocol(_host, _port, _connectTimeout, _readTimeout, _keepAlive, _reuseConnections)
  }
}

object TcpProtocolBuilder {
  def tcp(): TcpProtocolBuilder = new TcpProtocolBuilder()
}
