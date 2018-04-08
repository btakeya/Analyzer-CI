package com.kstreee.ci.analyzer.checkstyle

import com.kstreee.ci.analyzer.AnalyzerConfig

case class CheckstyleAnalyzerConfig(analysisCmd: Seq[String],
                                    reportFormat: CheckstyleAnalyzerReportFormat.ReportType) extends AnalyzerConfig {
  override val name: String = "checkstyle"
}