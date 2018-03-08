package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.coordinator.CoordinatorConfig
import com.kstreee.ci.coordinator.cli.CLICoordinatorConfig
import com.kstreee.ci.storage.ConfigLoad
import play.api.libs.json.{JsPath, JsResult, JsValue, Reads}

import scala.concurrent.{ExecutionContext, Future}

object CoordinatorConfigLoad extends ConfigLoad {
  override type T = JsValue
  override type U = CoordinatorConfig
  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    val config =
      for {
        name <- (JsPath \ "name").read[String].reads(data)
        config <- loadConfigByName(name, data)
      } yield config
    Future(traceJsResult(config).asOpt)
  }

  private[json] val cliName: String = "cli"
  private[json] val cliReads: Reads[CoordinatorConfig] =
    (JsPath \ "timeout_seconds").read[Int].map(timeoutSeconds => CLICoordinatorConfig(timeoutSeconds))

  private[json] def loadConfigByName(name: String, data: T): JsResult[U] = {
    name match {
      case _ if cliName.equalsIgnoreCase(name) => cliReads.reads(data)
      case _ => throw new NotImplementedError(s"Not implemented, $name")
    }
  }
}