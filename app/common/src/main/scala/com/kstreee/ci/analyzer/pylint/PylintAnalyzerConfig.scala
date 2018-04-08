package com.kstreee.ci.analyzer.pylint

import com.kstreee.ci.analyzer.AnalyzerConfig

case class PylintAnalyzerConfig(analysisCmd: Seq[String],
                                reportFormat: PylintAnalyzerReportFormat.ReportType) extends AnalyzerConfig {
  override val name: String = "pylint"
}