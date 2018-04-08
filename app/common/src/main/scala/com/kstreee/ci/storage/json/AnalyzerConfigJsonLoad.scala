package com.kstreee.ci.storage.json

import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzerConfig, PylintAnalyzerReportFormat}
import com.kstreee.ci.analyzer.checkstyle.{CheckstyleAnalyzerConfig, CheckstyleAnalyzerReportFormat}
import com.typesafe.scalalogging.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._

object AnalyzerConfigJsonLoad extends ConfigJsonLoad {
  private val logger = Logger[this.type]

  override type U = AnalyzerConfig

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

  def loadConfigByName(name: String, data: T): JsResult[U] = {
    name match {
      case _ if pylintName.equalsIgnoreCase(name) => pylintReads.reads(data)
      case _ if checkstyleName.equalsIgnoreCase(name) => checkstyleReads.reads(data)
      case _ =>
        logger.warn(s"Not implemented, $name")
        JsError(s"Not implemented, $name")
    }
  }
}