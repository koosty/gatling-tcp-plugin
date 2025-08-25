package com.github.koosty.gatling.tcp

import com.github.koosty.gatling.tcp.javaapi.TcpRequestActionBuilder.LengthHeaderType
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

class TcpRequestActionBuilderSpec extends AnyFlatSpec with Matchers with MockitoSugar{
  it should "store default parameters correctly" in {
    val builder = TcpRequestActionBuilder(
      requestName = "default-request",
      message = Array[Byte](1, 2, 3)
    )

    builder.requestName shouldBe "default-request"
    builder.message shouldEqual Array[Byte](1, 2, 3)
    builder.addLengthHeader shouldBe false
    builder.reuseConnection shouldBe false
    builder.connectionKey shouldBe "default"
    builder.validators should not be null
  }

  it should "store custom parameters correctly" in {
    val javaValidators = java.util.Arrays.asList[java.util.function.Function[Array[Byte], java.lang.Boolean]](
      (bytes: Array[Byte]) => bytes.nonEmpty,
      (bytes: Array[Byte]) => bytes.length == 3
    )

    val builder = TcpRequestActionBuilder(
      requestName = "custom-request",
      message = Array[Byte](1, 2, 3),
      addLengthHeader = true,
      lengthHeaderType = LengthHeaderType.FOUR_BYTE_BIG_ENDIAN,
      validators = javaValidators,
      connectionKey = "custom-connection"
    )

    builder.requestName shouldBe "custom-request"
    builder.addLengthHeader shouldBe true
    builder.lengthHeaderType shouldBe LengthHeaderType.FOUR_BYTE_BIG_ENDIAN
    builder.reuseConnection shouldBe false
    builder.connectionKey shouldBe "custom-connection"
    builder.validators.size() shouldBe 2
  }

  it should "handle Java to Scala validator conversion correctly" in {
    val javaValidators = java.util.Arrays.asList[java.util.function.Function[Array[Byte], java.lang.Boolean]](
      (bytes: Array[Byte]) => bytes.nonEmpty,
      (bytes: Array[Byte]) => bytes.length == 3
    )

    val builder = TcpRequestActionBuilder(
      requestName = "validator-test",
      message = Array[Byte](1, 2, 3),
      validators = javaValidators
    )

    // Test validators work as expected
    val validator1 = builder.validators.get(0)
    val validator2 = builder.validators.get(1)

    validator1.apply(Array[Byte](1, 2, 3)) shouldBe true
    validator1.apply(Array[Byte]()) shouldBe false

    validator2.apply(Array[Byte](1, 2, 3)) shouldBe true
    validator2.apply(Array[Byte](1, 2)) shouldBe false
  }
}
