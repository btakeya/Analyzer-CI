package com.kstreee.ci.analyzer.checkstyle

import com.kstreee.ci.analyzer.AnalyzerReportFormat
import com.typesafe.scalalogging.Logger

object CheckstyleAnalyzerReportFormat extends AnalyzerReportFormat {
  private val logger: Logger = Logger[this.type]

  def ofString(format: String): ReportType = {
    (if (format != null) format.toLowerCase() else "") match {
      case _ =>
        logger.warn(s"Not implemented report output format, $format")
        Unknown
    }
  }
}