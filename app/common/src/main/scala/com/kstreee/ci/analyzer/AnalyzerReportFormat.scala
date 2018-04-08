package com.kstreee.ci.analyzer

trait AnalyzerReportFormat extends Enumeration {
  type ReportType = Value
  val Unknown: ReportType = Value
  def ofString(format: String): ReportType
}