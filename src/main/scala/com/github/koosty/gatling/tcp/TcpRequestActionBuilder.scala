package com.github.koosty.gatling.tcp

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import scala.jdk.CollectionConverters._
import java.util.function.Function

class TcpRequestActionBuilder(
                               requestName: String,
                               message: Array[Byte],
                               expectResponse: Boolean,
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
      expectResponse,
      scalaValidators,
      components.protocol,
      ctx.coreComponents.statsEngine,
      ctx.coreComponents.clock,
      next
    )
  }
}