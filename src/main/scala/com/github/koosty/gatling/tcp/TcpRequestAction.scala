package com.github.koosty.gatling.tcp

import com.github.koosty.gatling.tcp.javaapi.TcpRequestBuilder.LengthHeaderType
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine

import java.io.{InputStream, OutputStream}
import java.net.{InetSocketAddress, Socket}

/**
 * Action for sending a TCP request and handling the response in a Gatling simulation.
 *
 * @param requestName Name of the request for reporting and session tracking.
 * @param message The message payload to send as a byte array.
 * @param addLengthHeader Whether to prepend a length header to the message.
 * @param lengthHeaderType The type of length header to use (big/little endian, 2/4 bytes).
 * @param validators List of functions to validate the response bytes.
 * @param reuseConnection Whether to reuse an existing TCP connection.
 * @param connectionKey Key to identify the connection in the session for reuse.
 * @param protocol TCP protocol configuration (host, port, timeouts, etc.).
 * @param statsEngine Gatling stats engine for logging results.
 * @param clock Clock instance for timing the request.
 * @param next The next action to execute in the scenario.
 */
case class TcpRequestAction(
                        requestName: String,
                        message: Array[Byte],
                        addLengthHeader: Boolean = false,
                        lengthHeaderType: LengthHeaderType,
                        validators: List[Function[Array[Byte], Boolean]] = List.empty,
                        reuseConnection: Boolean = false,
                        connectionKey: String = "default",
                        protocol: TcpProtocol,
                        statsEngine: StatsEngine,
                        clock: Clock,
                        next: Action
                      ) extends Action {
  /**
   * The name of this action, used for reporting.
   */
  override def name: String = requestName

  /**
   * Creates a length header for the message according to the specified header type.
   *
   * @param messageLength The length of the message to encode in the header.
   * @return Byte array representing the length header.
   */
  private def createLengthHeader(messageLength: Int): Array[Byte] = {
    lengthHeaderType match {
      case LengthHeaderType.TWO_BYTE_BIG_ENDIAN =>
        Array[Byte](
          ((messageLength >> 8) & 0xFF).toByte,  // High byte
          (messageLength & 0xFF).toByte          // Low byte
        )
      case LengthHeaderType.TWO_BYTE_LITTLE_ENDIAN =>
        Array[Byte](
          (messageLength & 0xFF).toByte,         // Low byte
          ((messageLength >> 8) & 0xFF).toByte   // High byte
        )
      case LengthHeaderType.FOUR_BYTE_BIG_ENDIAN =>
        Array[Byte](
          ((messageLength >> 24) & 0xFF).toByte,
          ((messageLength >> 16) & 0xFF).toByte,
          ((messageLength >> 8) & 0xFF).toByte,
          (messageLength & 0xFF).toByte
        )
      case LengthHeaderType.FOUR_BYTE_LITTLE_ENDIAN =>
        Array[Byte](
          (messageLength & 0xFF).toByte,
          ((messageLength >> 8) & 0xFF).toByte,
          ((messageLength >> 16) & 0xFF).toByte,
          ((messageLength >> 24) & 0xFF).toByte
        )
    }
  }

  private def readLengthFromHeader(headerBytes: Array[Byte]): Int = {
    lengthHeaderType match {
      case LengthHeaderType.TWO_BYTE_BIG_ENDIAN =>
        ((headerBytes(0) & 0xFF) << 8) | (headerBytes(1) & 0xFF)
      case LengthHeaderType.TWO_BYTE_LITTLE_ENDIAN =>
        (headerBytes(0) & 0xFF) | ((headerBytes(1) & 0xFF) << 8)
      case LengthHeaderType.FOUR_BYTE_BIG_ENDIAN =>
        ((headerBytes(0) & 0xFF) << 24) |
          ((headerBytes(1) & 0xFF) << 16) |
          ((headerBytes(2) & 0xFF) << 8) |
          (headerBytes(3) & 0xFF)
      case LengthHeaderType.FOUR_BYTE_LITTLE_ENDIAN =>
        (headerBytes(0) & 0xFF) |
          ((headerBytes(1) & 0xFF) << 8) |
          ((headerBytes(2) & 0xFF) << 16) |
          ((headerBytes(3) & 0xFF) << 24)
    }
  }

  private def getHeaderSize: Int = {
    lengthHeaderType match {
      case LengthHeaderType.TWO_BYTE_BIG_ENDIAN | LengthHeaderType.TWO_BYTE_LITTLE_ENDIAN => 2
      case LengthHeaderType.FOUR_BYTE_BIG_ENDIAN | LengthHeaderType.FOUR_BYTE_LITTLE_ENDIAN => 4
    }
  }

  private def createConnection(keepAlive: Boolean, readTimeout: Int, connectTimeout: Int, inetSocketAddress: InetSocketAddress): Socket = {
    val socket = new Socket()
    socket.setKeepAlive(keepAlive)
    socket.setSoTimeout(readTimeout)
    socket.connect(inetSocketAddress, connectTimeout)
    socket
  }
  override def execute(session: Session): Unit = {
    val requestId = s"${session.userId}-${System.nanoTime()}"
    val isa = new InetSocketAddress(protocol.host, protocol.port)
    logger.debug(s"[$requestId] Executing TCP request: $requestName")
    var socket: Socket = null
    var shouldCloseSocket = false
    var finalSession = session

    try {
      val start = clock.nowMillis

      val messageToSend = if (addLengthHeader) {
        val lengthHeader = createLengthHeader(message.length)
        val result = new Array[Byte](lengthHeader.length + message.length)
        System.arraycopy(lengthHeader, 0, result, 0, lengthHeader.length)
        System.arraycopy(message, 0, result, lengthHeader.length, message.length)
        result
      } else {
        message
      }

      // Create socket with timeout
      socket = if (reuseConnection) {
        session(s"tcp.connection.$connectionKey").asOption[Socket] match {
          case Some(existingSocket) if !existingSocket.isClosed && existingSocket.isConnected=>
            logger.debug(s"[$requestId] Reusing existing connection")
            existingSocket
          case _ =>
            logger.debug(s"[$requestId] Creating new connection to ${protocol.host}:${protocol.port} with timeout ${protocol.connectTimeout}ms")
            createConnection(protocol.keepAlive, protocol.readTimeout, protocol.connectTimeout, isa)
        }
      } else {
        shouldCloseSocket = true // Always close if not reusing
        logger.debug(s"[$requestId] Creating new connection to ${protocol.host}:${protocol.port} with timeout ${protocol.connectTimeout}ms")
        createConnection(protocol.keepAlive, protocol.readTimeout, protocol.connectTimeout, isa)
      }

      val out: OutputStream = socket.getOutputStream
      val in: InputStream = socket.getInputStream
      // Send the message (with or without length header)
      logger.debug(s"[$requestId] Sending request of length ${messageToSend.length} bytes")
      out.write(messageToSend)
      out.flush()
      // Read response
      logger.debug(s"[$requestId] Waiting for response")
      var totalBytesRead = 0
      val responseBytes = if (addLengthHeader) {
        // Read response length header first
        val headerSize = getHeaderSize
        val responseHeader = new Array[Byte](headerSize)
        val headerBytesRead = in.read(responseHeader, 0, headerSize)

        if (headerBytesRead != headerSize) {
          throw new Exception(s"Failed to read response length header (expected $headerSize bytes, got $headerBytesRead)")
        }

        // Calculate response length from header
        val responseLength = readLengthFromHeader(responseHeader)

        if (responseLength < 0 || responseLength > 1024 * 1024) { // 1MB safety limit
          throw new Exception(s"Invalid response length: $responseLength")
        }

        // Read the actual response
        val buffer = new Array[Byte](responseLength)

        while (totalBytesRead < responseLength) {
          val bytesRead = in.read(buffer, totalBytesRead, responseLength - totalBytesRead)
          if (bytesRead == -1) {
            throw new Exception("Connection closed before receiving complete response")
          }
          totalBytesRead += bytesRead
        }

        java.util.Arrays.copyOf(buffer, totalBytesRead)
      } else {
        // Read all available data (no length header expected)
        logger.debug(s"[$requestId] Request doesn't have a length header, reading all available data")
        /** Read response data from the input stream using a fixed-size buffer.
         * Uses an 8KB buffer to efficiently read data chunks from the socket.
         */
        val buffer = new Array[Byte](8192) // 8KB buffer
        val bytesRead = in.read(buffer)
        if (bytesRead == -1) {
          throw new Exception("No response received")
        }
        java.util.Arrays.copyOf(buffer, bytesRead)
      }

      val end = clock.nowMillis

      /** Validate the response using configured validators.
       * Each validator is applied to the response bytes and returns a tuple of:
       * - Boolean: whether validation passed
       * - Option[String]: optional error message if validation failed
       */
      val validationResults = validators.map { validator =>
        try {
          validator.apply(responseBytes) -> None
        } catch {
          case e: Exception => false -> Some(s"Validation error: ${e.getMessage}")
        }
      }
      logger.debug(s"[$requestId] Validating response with ${validationResults.size} validators")
      val validationsPassed = validationResults.forall(_._1)
      val validationErrors = validationResults.flatMap(_._2)

      /** Handle the response based on validation results.
       * If all validations pass:
       * - Log successful response to stats engine
       * - Update session with response data and metrics
       * If any validation fails:
       * - Log warning with validation error details
       */
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
        logger.debug(s"[$requestId] Request successful, response length: $totalBytesRead")
        // Update session with response data AND connection
        finalSession = session
          .set(s"$requestName.response", responseBytes)
          .set(s"$requestName.bytesReceived", totalBytesRead)
          .set(s"$requestName.bytesSent", message.length)

        // Store connection for reuse if needed
        if (reuseConnection && !socket.isClosed) {
          finalSession = finalSession.set(s"tcp.connection.$connectionKey", socket)
        } else {
          shouldCloseSocket = true
        }
      } else {
        logger.warn(s"[$requestId] Response validation failed: ${validationErrors.mkString(", ")}")
        // Handle validation failure (same logic as yours)
        shouldCloseSocket = true // Close connection on validation failure
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
        finalSession = session
          .set(s"$requestName.response", responseBytes)
          .set(s"$requestName.responseString", new String(responseBytes))
          .set(s"$requestName.validationError", errorMessage)
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
        finalSession = session.markAsFailed
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
        finalSession = session.markAsFailed
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
        finalSession = session.markAsFailed
    } finally {
      // Clean up socket if needed
      if (socket != null && shouldCloseSocket) {
        try {
          socket.close()
          // Remove from session if it was stored
          if (reuseConnection) {
            finalSession = finalSession.remove(s"tcp.connection.$connectionKey")
          }
        } catch {
          case e: Exception =>
            logger.warn(s"[$requestId] Error closing socket: ${e.getMessage}")
        }
      }
      // Always pass the final session to next action
      next ! finalSession
    }
  }
}
