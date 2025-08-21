package com.github.koosty.gatling.tcp

import com.github.koosty.gatling.tcp.javaapi.TcpRequestBuilder.LengthHeaderType
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext

import java.util.function.Function
import scala.jdk.CollectionConverters._

/**
 * Builder for creating TCP request actions in Gatling scenarios.
 *
 * @param requestName Name of the request.
 * @param message Byte array representing the message to send.
 * @param addLengthHeader Whether to add a length header to the message.
 * @param lengthHeaderType Type of length header to use.
 * @param validators List of Java functions to validate the response.
 */
class TcpRequestActionBuilder(
                               requestName: String,
                               message: Array[Byte],
                               addLengthHeader: Boolean = false,
                               lengthHeaderType: LengthHeaderType = LengthHeaderType.TWO_BYTE_BIG_ENDIAN,
                               validators: java.util.List[Function[Array[Byte], java.lang.Boolean]] = new java.util.ArrayList()
                             ) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val components = ctx.protocolComponentsRegistry
      .components(TcpProtocol.TcpProtocolKey)
      .asInstanceOf[TcpComponents]
    // Convert Java Functions to Scala Function1
    val scalaValidators: List[Array[Byte] => Boolean] = validators.asScala.toList.map { javaFunc =>
      (bytes: Array[Byte]) => javaFunc.apply(bytes)
    }
    new TcpRequestAction(
      requestName,
      message,
      addLengthHeader,
      lengthHeaderType,
      scalaValidators,
      components.protocol,
      ctx.coreComponents.statsEngine,
      ctx.coreComponents.clock,
      next
    )
  }
}