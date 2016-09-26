package com.kstreee.ci.analyzer.pylint

import com.kstreee.ci.analyzer.AnalyzerSummary

case class PylintAnalyzerSummary(override val items: List[PylintAnalyzerSummaryItem]) extends AnalyzerSummary {
  override type T = PylintAnalyzerSummaryItem
}