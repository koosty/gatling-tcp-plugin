package com.github.koosty.gatling.tcp

import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

import java.io.{InputStream, OutputStream}
import java.net.{InetSocketAddress, Socket}

class TcpRequestAction(
                        requestName: String,
                        message: Array[Byte],
                        expectResponse: Boolean,
                        validators: List[Function[Array[Byte], Boolean]] = List.empty,
                        protocol: TcpProtocol,
                        statsEngine: StatsEngine,
                        clock: Clock,
                        next: Action
                      ) extends Action {
  override def name: String = requestName

  override def execute(session: Session): Unit = {
    val requestId = session.userId + "-" + System.nanoTime()
    logger.debug(s"[$requestId] Executing TCP request: $requestName")
    var socket: Socket = null
    try {
      val start = clock.nowMillis

      // Create length header (2 bytes)
      val messageLength = message.length
      val lengthHeader = Array[Byte](
        ((messageLength >> 8) & 0xFF).toByte,  // High byte
        (messageLength & 0xFF).toByte          // Low byte
      )

      // Create socket with timeout
      socket = new Socket()
      socket.setKeepAlive(protocol.keepAlive)
      socket.setSoTimeout(protocol.readTimeout)
      socket.connect(new InetSocketAddress(protocol.host, protocol.port), protocol.connectTimeout)

      val out: OutputStream = socket.getOutputStream
      val in: InputStream = socket.getInputStream

      // Send length header followed by message
      out.write(lengthHeader)    // Send 2-byte length header
      out.write(message)         // Send actual message
      out.flush()

      // Read response length header first (2 bytes)
      val responseHeader = new Array[Byte](2)
      val headerBytesRead = in.read(responseHeader, 0, 2)

      if (headerBytesRead != 2) {
        throw new IllegalArgumentException("Failed to read response length header")
      }

      // Calculate response length from header
      val responseLength = ((responseHeader(0) & 0xFF) << 8) | (responseHeader(1) & 0xFF)

      // Read the actual response
      val buffer = new Array[Byte](responseLength)
      var totalBytesRead = 0
      while (totalBytesRead < responseLength) {
        val bytesRead = in.read(buffer, totalBytesRead, responseLength - totalBytesRead)
        if (bytesRead == -1) {
          throw new IllegalStateException("Connection closed before receiving complete response")
        }
        totalBytesRead += bytesRead
      }

      val response = java.util.Arrays.copyOf(buffer, totalBytesRead)
      val end = clock.nowMillis
      // Run validations
      val validationResults = validators.map { validator =>
        try {
          validator.apply(response) -> None
        } catch {
          case e: Exception => false -> Some(s"Validation error: ${e.getMessage}")
        }
      }
      val validationsPassed = validationResults.forall(_._1)
      val validationErrors = validationResults.flatMap(_._2)
      if(validationsPassed) {
        statsEngine.logResponse(
          scenario = session.scenario,
          groups = session.groups,
          requestName = requestName,
          startTimestamp = start,
          endTimestamp = end,
          status = OK,
          responseCode = None,
          message = None
        )
        next ! session.set(s"${requestName}.response", response)
          .set(s"${requestName}.bytesReceived", totalBytesRead)
          .set(s"${requestName}.bytesSent", message.length)
        logger.debug(s"[$requestId] Request successful, response length: $totalBytesRead")
      } else {
        val errorMessage = if (validationErrors.nonEmpty) {
          validationErrors.mkString("; ")
        } else {
          "Response validation failed"
        }

        statsEngine.logResponse(
          scenario = session.scenario,
          groups = session.groups,
          requestName = requestName,
          startTimestamp = start,
          endTimestamp = end,
          status = KO,
          responseCode = None,
          message = Some(errorMessage)
        )

        next ! session.set(s"${requestName}.response", response)
          .set(s"${requestName}.responseString", new String(response))
          .set(s"${requestName}.validationError", errorMessage)
          .markAsFailed
      }

    } catch {
      case e: java.net.SocketTimeoutException =>
        logger.warn(s"[$requestId] Request timeout: ${e.getMessage}")
        statsEngine.logResponse(
          scenario = session.scenario,
          groups = session.groups,
          requestName = requestName,
          startTimestamp = clock.nowMillis,
          endTimestamp = clock.nowMillis,
          status = KO,
          responseCode = None,
          message = Some("Timeout"))
      case e: java.net.ConnectException =>
        logger.error(s"[$requestId] Connection failed: ${e.getMessage}")
        statsEngine.logResponse(
          scenario = session.scenario,
          groups = session.groups,
          requestName = requestName,
          startTimestamp = clock.nowMillis,
          endTimestamp = clock.nowMillis,
          status = KO,
          responseCode = None,
          message = Some("Connection failed")
        )
      case e: Exception =>
        logger.error(s"[$requestId] Unexpected error: ${e.getMessage}", e)
        statsEngine.logResponse(
          scenario = session.scenario,
          groups = session.groups,
          requestName = requestName,
          startTimestamp = clock.nowMillis,
          endTimestamp = clock.nowMillis,
          status = KO,
          responseCode = None,
          message = Some(e.getMessage)
        )
        next ! session.markAsFailed
    } finally {
      if (socket != null) socket.close()
    }
  }
}
