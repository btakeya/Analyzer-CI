package com.kstreee.ci.storage.json

import com.kstreee.ci.coordinator.CoordinatorConfig
import com.kstreee.ci.coordinator.cli.CLICoordinatorConfig
import com.kstreee.ci.coordinator.file.FileCoordinatorConfig
import com.typesafe.scalalogging.Logger
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

object CoordinatorConfigJsonLoad extends ConfigJsonLoad {
  private val logger = Logger[this.type]

  override type U = CoordinatorConfig

  // To support Java
  override def loadByString(data: String)(implicit ctx: ExecutionContext): Future[Option[U]] = super.loadByString(data)

  private[json] val cliName: String = "cli"
  private[json] val cliReads: Reads[CoordinatorConfig] =
    (JsPath \ "timeout_seconds").readNullable[Int].map(CLICoordinatorConfig.apply)
  private[json] val fileName: String = "file"
  private[json] val fileReads: Reads[CoordinatorConfig] =
    (JsPath \ "report_path").read[String].map(FileCoordinatorConfig.apply)

  def loadConfigByName(name: String, data: T): JsResult[U] = {
    name match {
      case _ if cliName.equalsIgnoreCase(name) => cliReads.reads(data)
      case _ if fileName.equalsIgnoreCase(name) => fileReads.reads(data)
      case _ =>
        logger.warn(s"Not implemented, $name")
        JsError(s"Not implemented, $name")
    }
  }
}