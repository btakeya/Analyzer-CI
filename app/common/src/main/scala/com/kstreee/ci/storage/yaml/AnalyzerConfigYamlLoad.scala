package com.kstreee.ci.storage.yaml

import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzerConfig, PylintAnalyzerReportFormat}
import com.typesafe.scalalogging.Logger
import net.jcazevedo.moultingyaml._

import scala.concurrent.{ExecutionContext, Future}

object AnalyzerConfigYamlLoad extends ConfigYamlLoad {
  private val logger: Logger = Logger[this.type]

  override type U = AnalyzerConfig

  // To support Java
  override def loadByString(data: String)(implicit ctx: ExecutionContext): Future[Option[U]] = super.loadByString(data)

  private[yaml] val pylintName: String = "pylint"
  private[yaml] def pylintReads(data: T): Option[PylintAnalyzerConfig] = {
    data.asYamlObject.getFields(
      YamlString("analysis_cmd"),
      YamlString("report_format")) match {
      case Seq(
      YamlString(analysisCmd),
      YamlString(reportFormat)) =>
        Some(PylintAnalyzerConfig(
          analysisCmd.split("\\s+"),
          PylintAnalyzerReportFormat.ofString(reportFormat)))
      case _ =>
        logger.error(s"Failed to parse config for pylint, $data")
        None
    }
  }

  private[yaml] val checkstyleName: String = "checkstyle"
  private[yaml] def checkstyleReads(data: T): Option[PylintAnalyzerConfig] = {
    data.asYamlObject.getFields(
      YamlString("analysis_cmd"),
      YamlString("report_format")) match {
      case Seq(
      YamlString(analysisCmd),
      YamlString(reportFormat)) =>
        Some(PylintAnalyzerConfig(
          analysisCmd.split("\\s+"),
          PylintAnalyzerReportFormat.ofString(reportFormat)))
      case _ =>
        logger.error(s"Failed to parse config for checkstyle, $data")
        None
    }
  }

  def loadConfigByName(name: String, data: T): Option[U] = {
    name match {
      case _ if pylintName.equalsIgnoreCase(name) => pylintReads(data)
      case _ if checkstyleName.equalsIgnoreCase(name) => checkstyleReads(data)
      case _ =>
        logger.warn(s"Not implemented, $name")
        None
    }
  }
}