package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzerConfig, PylintAnalyzerReportFormat}
import com.kstreee.ci.analyzer.checkstyle.{CheckstyleAnalyzerConfig, CheckstyleAnalyzerReportFormat}
import com.kstreee.ci.storage.ConfigLoad
import com.typesafe.scalalogging.Logger
import play.api.libs.json.{JsError, JsPath, JsResult, Reads}
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}

object AnalyzerConfigLoad extends ConfigLoad {
  private val logger = Logger[this.type]

  override type U = AnalyzerConfig
  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    val config =
      for {
        name <- (JsPath \ "name").read[String].reads(data)
        config <- loadConfigByName(name, data)
      } yield config
    Future(asOption(config, (e: JsError) => logger.error("Failed to parse config,", e)))
  }

  private[json] val pylintName: String = "pylint"
  private[json] implicit val pylintReads: Reads[AnalyzerConfig] = (
    (JsPath \ "analysis_cmd").read[String].map[Seq[String]](s => s.trim.split("\\s+")) and
      (JsPath \ "report_format").read[String].map[PylintAnalyzerReportFormat.ReportType](PylintAnalyzerReportFormat.ofString)
    ) (PylintAnalyzerConfig.apply _)

  private[json] val checkstyleName: String = "checkstyle"
  private[json] implicit val checkstyleReads: Reads[AnalyzerConfig] = (
    (JsPath \ "analysis_cmd").read[String].map[Seq[String]](s => s.trim.split("\\s+")) and
      (JsPath \ "report_format").read[String].map[CheckstyleAnalyzerReportFormat.ReportType](CheckstyleAnalyzerReportFormat.ofString)
    ) (CheckstyleAnalyzerConfig.apply _)

  private[json] def loadConfigByName(name: String, data: T): JsResult[U] = {
    name match {
      case _ if pylintName.equalsIgnoreCase(name) => pylintReads.reads(data)
      case _ if checkstyleName.equalsIgnoreCase(name) => checkstyleReads.reads(data)
      case _ =>
        logger.warn(s"Not implemented, $name")
        JsError(s"Not implemented, $name")
    }
  }
}