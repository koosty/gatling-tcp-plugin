package com.github.koosty.gatling.tcp

import com.github.koosty.gatling.tcp.javaapi.TcpRequestBuilder.LengthHeaderType
import io.gatling.commons.stats.{KO, OK, Status}
import io.gatling.commons.util.Clock
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.scalatest.concurrent.Eventually.{eventually, timeout}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.mockito.MockitoSugar

import java.net.ServerSocket
import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TcpRequestActionSpec extends AnyFlatSpec with Matchers with MockitoSugar {

  // Test fixtures
  val requestName = "test-request"
  val testMessage: Array[Byte] = "Hello World".getBytes
  val mockClock: Clock = mock[Clock]
  val mockNextAction: Action = mock[Action]

  // Create real session instead of mocking
  def createTestSession(userId: Long = 1, scenario: String = "test-scenario"): Session = {
    Session(
      scenario = scenario,
      userId = userId,
      eventLoop = null // Not needed for these tests
    )
  }
  // Helper to create TcpProtocol
  def createTcpProtocol(host: String = "localhost", port: Int = 0): TcpProtocol = {
    TcpProtocol(
      host = host,
      port = port,
      connectTimeout = 1000,
      readTimeout = 1000,
      keepAlive = false
    )
  }

  behavior of "TcpRequestAction"

  it should "have correct name" in {
    val mockStatsEngine = mock[StatsEngine]
    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    action.name shouldBe requestName
  }

  it should "create correct length header for TWO_BYTE_BIG_ENDIAN" in {
    val mockStatsEngine = mock[StatsEngine]
    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      addLengthHeader = true,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    // Use reflection to access private method for testing
    val method = action.getClass.getDeclaredMethod("createLengthHeader", classOf[Int])
    method.setAccessible(true)

    val header = method.invoke(action, Integer.valueOf(256)).asInstanceOf[Array[Byte]]
    header shouldBe Array[Byte](1, 0) // 256 in big endian: high byte = 1, low byte = 0
  }

  it should "create correct length header for TWO_BYTE_LITTLE_ENDIAN" in {
    val mockStatsEngine = mock[StatsEngine]
    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      addLengthHeader = true,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_LITTLE_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val method = action.getClass.getDeclaredMethod("createLengthHeader", classOf[Int])
    method.setAccessible(true)

    val header = method.invoke(action, Integer.valueOf(256)).asInstanceOf[Array[Byte]]
    header shouldBe Array[Byte](0, 1) // 256 in little endian: low byte = 0, high byte = 1
  }

  it should "create correct length header for FOUR_BYTE_BIG_ENDIAN" in {
    val mockStatsEngine = mock[StatsEngine]
    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      addLengthHeader = true,
      lengthHeaderType = LengthHeaderType.FOUR_BYTE_BIG_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val method = action.getClass.getDeclaredMethod("createLengthHeader", classOf[Int])
    method.setAccessible(true)

    val header = method.invoke(action, Integer.valueOf(0x12345678)).asInstanceOf[Array[Byte]]
    header shouldBe Array[Byte](0x12, 0x34, 0x56, 0x78)
  }

  it should "create correct length header for FOUR_BYTE_LITTLE_ENDIAN" in {
    val mockStatsEngine = mock[StatsEngine]
    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      addLengthHeader = true,
      lengthHeaderType = LengthHeaderType.FOUR_BYTE_LITTLE_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val method = action.getClass.getDeclaredMethod("createLengthHeader", classOf[Int])
    method.setAccessible(true)

    val header = method.invoke(action, Integer.valueOf(0x12345678)).asInstanceOf[Array[Byte]]
    header shouldBe Array[Byte](0x78, 0x56, 0x34, 0x12)
  }

  it should "read length from TWO_BYTE_BIG_ENDIAN header correctly" in {
    val mockStatsEngine = mock[StatsEngine]
    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val method = action.getClass.getDeclaredMethod("readLengthFromHeader", classOf[Array[Byte]])
    method.setAccessible(true)

    val length = method.invoke(action, Array[Byte](1, 0)).asInstanceOf[Int]
    length shouldBe 256
  }

  it should "read length from TWO_BYTE_LITTLE_ENDIAN header correctly" in {
    val mockStatsEngine = mock[StatsEngine]
    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_LITTLE_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val method = action.getClass.getDeclaredMethod("readLengthFromHeader", classOf[Array[Byte]])
    method.setAccessible(true)

    val length = method.invoke(action, Array[Byte](0, 1)).asInstanceOf[Int]
    length shouldBe 256
  }

  it should "return correct header size for different types" in {
    val mockStatsEngine = mock[StatsEngine]
    val twoByteAction = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val fourByteAction = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.FOUR_BYTE_BIG_ENDIAN,
      protocol = createTcpProtocol(),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val twoByteMethod = twoByteAction.getClass.getDeclaredMethod("getHeaderSize")
    twoByteMethod.setAccessible(true)

    val fourByteMethod = fourByteAction.getClass.getDeclaredMethod("getHeaderSize")
    fourByteMethod.setAccessible(true)

    twoByteMethod.invoke(twoByteAction).asInstanceOf[Int] shouldBe 2
    fourByteMethod.invoke(fourByteAction).asInstanceOf[Int] shouldBe 4
  }

  it should "successfully execute request without length header" in {
    val mockStatsEngine = mock[StatsEngine]
    // Setup mock server
    val serverSocket = new ServerSocket(0)
    val port = serverSocket.getLocalPort
    val responseData = "Response".getBytes

    // Setup mocks
    when(mockClock.nowMillis).thenReturn(1000L, 2000L)

    val latch = new CountDownLatch(1)

    // Start mock server
    Future {
      val clientSocket = serverSocket.accept()
      val in = clientSocket.getInputStream
      val out = clientSocket.getOutputStream

      // Read request
      val buffer = new Array[Byte](1024)
      val bytesRead = in.read(buffer)

      // Send response
      out.write(responseData)
      out.flush()

      clientSocket.close()
      latch.countDown()
    }

    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      protocol = createTcpProtocol(port = port),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )
    val session = createTestSession()
    // Execute action
    action.execute(session)

    // Wait for server to complete
    latch.await(2, TimeUnit.SECONDS) shouldBe true

    // Verify stats engine was called with OK status
    verify(mockStatsEngine).logResponse(
      scenario = "test-scenario",
      groups = Nil,
      requestName = requestName,
      startTimestamp = 1000L,
      endTimestamp = 2000L,
      status = OK,
      responseCode = None,
      message = None
    )

    // Verify next action was called
    verify(mockNextAction).!(any[Session])

    serverSocket.close()
  }

  it should "successfully execute request with length header" in {
    val mockStatsEngine = mock[StatsEngine]
    val serverSocket = new ServerSocket(0)
    val port = serverSocket.getLocalPort
    val responseData = "Response".getBytes

    when(mockClock.nowMillis).thenReturn(1000L, 2000L)

    val latch = new CountDownLatch(1)

    Future {
      val clientSocket = serverSocket.accept()
      val in = clientSocket.getInputStream
      val out = clientSocket.getOutputStream

      // Read request with length header
      val headerBuffer = new Array[Byte](2)
      in.read(headerBuffer)
      val messageLength = ((headerBuffer(0) & 0xFF) << 8) | (headerBuffer(1) & 0xFF)
      val messageBuffer = new Array[Byte](messageLength)
      in.read(messageBuffer)

      // Send response with length header
      val responseLength = responseData.length
      val responseHeader = Array[Byte](
        ((responseLength >> 8) & 0xFF).toByte,
        (responseLength & 0xFF).toByte
      )
      out.write(responseHeader)
      out.write(responseData)
      out.flush()

      clientSocket.close()
      latch.countDown()
    }

    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      addLengthHeader = true,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      protocol = createTcpProtocol(port = port),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )
    val session = createTestSession()
    action.execute(session)
    latch.await(2, TimeUnit.SECONDS) shouldBe true

    verify(mockStatsEngine).logResponse(
      scenario = "test-scenario",
      groups = Nil,
      requestName = requestName,
      startTimestamp = 1000L,
      endTimestamp = 2000L,
      status = OK,
      responseCode = None,
      message = None
    )

    serverSocket.close()
  }

  it should "handle validation failure" in {
    val mockStatsEngine = mock[StatsEngine]
    val serverSocket = new ServerSocket(0)
    val port = serverSocket.getLocalPort
    val responseData = "Invalid Response".getBytes

    when(mockClock.nowMillis).thenReturn(1000L, 2000L)

    val latch = new CountDownLatch(1)

    Future {
      val clientSocket = serverSocket.accept()
      val in = clientSocket.getInputStream
      val out = clientSocket.getOutputStream

      val buffer = new Array[Byte](1024)
      in.read(buffer)
      out.write(responseData)
      out.flush()

      clientSocket.close()
      latch.countDown()
    }

    // Validator that always fails
    val failingValidator: Function[Array[Byte], Boolean] = _ => false

    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      validators = List(failingValidator),
      protocol = createTcpProtocol(port = port),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )
    val session = createTestSession()
    action.execute(session)
    latch.await(2, TimeUnit.SECONDS) shouldBe true

    verify(mockStatsEngine).logResponse(
      scenario = "test-scenario",
      groups = Nil,
      requestName = requestName,
      startTimestamp = 1000L,
      endTimestamp = 2000L,
      status = KO,
      responseCode = None,
      message = Some("Response validation failed")
    )

    serverSocket.close()
  }

  it should "handle connection timeout" in {
    val mockStatsEngine = mock[StatsEngine]
    when(mockClock.nowMillis).thenReturn(1000L)

    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      protocol = TcpProtocol(
        host = "192.0.2.1", // Non-routable IP for timeout
        port = 12345,
        connectTimeout = 100,
        readTimeout = 100,
        keepAlive = false
      ),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )
    val session = createTestSession()
    action.execute(session)

    verify(mockStatsEngine).logResponse(
      scenario = "test-scenario",
      groups = Nil,
      requestName = requestName,
      startTimestamp = 1000L,
      endTimestamp = 1000L,
      status = KO,
      responseCode = None,
      message = Some("Timeout")
    )
  }

  it should "handle validator exception" in {
    val mockStatsEngine = mock[StatsEngine]
    val serverSocket = new ServerSocket(0)
    val port = serverSocket.getLocalPort
    val responseData = "Response".getBytes

    when(mockClock.nowMillis).thenReturn(1000L, 2000L)

    val latch = new CountDownLatch(1)

    Future {
      val clientSocket = serverSocket.accept()
      val in = clientSocket.getInputStream
      val out = clientSocket.getOutputStream

      val buffer = new Array[Byte](1024)
      in.read(buffer)
      out.write(responseData)
      out.flush()

      clientSocket.close()
      latch.countDown()
    }

    // Validator that throws exception
    val throwingValidator: Function[Array[Byte], Boolean] = _ => throw new RuntimeException("Validation error")

    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      validators = List(throwingValidator),
      protocol = createTcpProtocol(port = port),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val session = createTestSession()
    action.execute(session)
    latch.await(2, TimeUnit.SECONDS) shouldBe true

    verify(mockStatsEngine).logResponse(
      scenario = "test-scenario",
      groups = Nil,
      requestName = requestName,
      startTimestamp = 1000L,
      endTimestamp = 2000L,
      status = KO,
      responseCode = None,
      message = Some("Validation error: Validation error")
    )

    serverSocket.close()
  }

  it should "handle successful validation with multiple validators" in {
    val mockStatsEngine = mock[StatsEngine]
    val serverSocket = new ServerSocket(0)
    val port = serverSocket.getLocalPort
    val responseData = "ValidResponse".getBytes

    when(mockClock.nowMillis).thenReturn(1000L, 2000L)

    val latch = new CountDownLatch(1)

    Future {
      val clientSocket = serverSocket.accept()
      val in = clientSocket.getInputStream
      val out = clientSocket.getOutputStream

      val buffer = new Array[Byte](1024)
      in.read(buffer)
      out.write(responseData)
      out.flush()

      clientSocket.close()
      latch.countDown()
    }

    // Multiple validators that pass
    val validator1: Function[Array[Byte], Boolean] = data => data.length > 0
    val validator2: Function[Array[Byte], Boolean] = data => new String(data).contains("Valid")

    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      validators = List(validator1, validator2),
      protocol = createTcpProtocol(port = port),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val session = createTestSession()
    action.execute(session)
    latch.await(2, TimeUnit.SECONDS) shouldBe true

    verify(mockStatsEngine).logResponse(
      scenario = "test-scenario",
      groups = Nil,
      requestName = requestName,
      startTimestamp = 1000L,
      endTimestamp = 2000L,
      status = OK,
      responseCode = None,
      message = None
    )

    serverSocket.close()
  }

  it should "handle connection reuse correctly" in {
    val mockStatsEngine = mock[StatsEngine]
    val serverSocket = new ServerSocket(0)
    val port = serverSocket.getLocalPort
    val responseData = "Response".getBytes

    when(mockClock.nowMillis).thenReturn(1000L, 2000L, 3000L, 4000L)

    val latch = new CountDownLatch(2)
    var capturedSession: Session = null

    // Capture the updated session
    when(mockNextAction.!(any[Session])).thenAnswer((invocation: InvocationOnMock) => {
      capturedSession = invocation.getArgument[Session](0)
    })

    Future {
      val clientSocket = serverSocket.accept()
      val in = clientSocket.getInputStream
      val out = clientSocket.getOutputStream

      try {
        println("Server: Waiting for first request")
        val buffer1 = new Array[Byte](1024)
        in.read(buffer1)
        out.write(responseData)
        out.flush()
        latch.countDown()
        println(s"Server: First request handled, latch: ${latch.getCount}")

        println("Server: Waiting for second request")
        val buffer2 = new Array[Byte](1024)
        in.read(buffer2)
        out.write(responseData)
        out.flush()
        latch.countDown()
        println(s"Server: Second request handled, latch: ${latch.getCount}")

      } catch {
        case e: Exception =>
          println(s"Server error: ${e.getMessage}")
          e.printStackTrace()
      } finally {
        clientSocket.close()
        println("Server: Connection closed")
      }
    }

    val action = TcpRequestAction(
      requestName = requestName,
      message = testMessage,
      lengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
      reuseConnection = true,
      connectionKey = "test-connection",
      protocol = createTcpProtocol(port = port),
      statsEngine = mockStatsEngine,
      clock = mockClock,
      next = mockNextAction
    )

    val initialSession = createTestSession()

    println("Test: Executing first request")
    action.execute(initialSession)

    // Wait for first request to complete and session to be captured
    eventually(timeout(Span(2, Seconds))) {
      capturedSession should not be null
    }

    println("Test: Executing second request")
    action.execute(capturedSession)

    val result = latch.await(5, TimeUnit.SECONDS)
    println(s"Test: Latch result: $result, count: ${latch.getCount}")

    result shouldBe true

    verify(mockStatsEngine, times(2)).logResponse(
      any[String], any[List[String]], any[String], any[Long], any[Long],
      any[Status], any[Option[String]], any[Option[String]]
    )

    serverSocket.close()
  }
}

