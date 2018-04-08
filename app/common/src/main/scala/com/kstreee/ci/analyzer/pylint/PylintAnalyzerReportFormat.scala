package com.kstreee.ci.analyzer.pylint

import com.kstreee.ci.analyzer.AnalyzerReportFormat
import com.typesafe.scalalogging.Logger

object PylintAnalyzerReportFormat extends AnalyzerReportFormat {
  private val logger: Logger = Logger[this.type]

  val Json, Parseable, MSVS: ReportType = Value

  def ofString(format: String): ReportType = {
    (if (format != null) format.toLowerCase() else "") match {
      case "json" => Json
      case "parseable" => Parseable
      case "msvs" => MSVS
      case _ =>
        logger.warn(s"Not implemented report output format, $format")
        Unknown
    }
  }
}