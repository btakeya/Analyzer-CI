package com.kstreee.ci.storage.yaml

import com.kstreee.ci.coordinator.CoordinatorConfig
import com.kstreee.ci.coordinator.cli.CLICoordinatorConfig
import com.kstreee.ci.coordinator.file.FileCoordinatorConfig
import com.typesafe.scalalogging.Logger
import net.jcazevedo.moultingyaml._

import scala.concurrent.{ExecutionContext, Future}

object CoordinatorConfigYamlLoad extends ConfigYamlLoad {
  private val logger: Logger = Logger[this.type]

  override type U = CoordinatorConfig

  // To support Java
  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = super.load(data)
  override def loadByString(data: String)(implicit ctx: ExecutionContext): Future[Option[U]] = super.loadByString(data)

  private[yaml] val cliName: String = "cli"
  private[yaml] def cliReads(data: T): Option[CoordinatorConfig] = {
    data.asYamlObject.getFields(YamlString("timeout_seconds")) match {
      case Seq(YamlNumber(timeoutSeconds)) => Some(CLICoordinatorConfig(Some(timeoutSeconds.toInt)))
      case _ => Some(CLICoordinatorConfig(None))
    }
  }
  private[yaml] val fileName: String = "file"
  private[yaml] def fileReads(data: T): Option[CoordinatorConfig] = {
    data.asYamlObject.getFields(YamlString("report_path")) match {
      case Seq(YamlString(reportPath)) => Some(FileCoordinatorConfig(reportPath))
      case _ =>
        logger.error("Failed to get report_path from file coordinator config")
        None
    }
  }

  def loadConfigByName(name: String, data: T): Option[U] = {
    name match {
      case _ if cliName.equalsIgnoreCase(name) => cliReads(data)
      case _ if fileName.equalsIgnoreCase(name) => fileReads(data)
      case _ =>
        logger.warn(s"Not implemented, $name , type : ${name.getClass}")
        None
    }
  }
}