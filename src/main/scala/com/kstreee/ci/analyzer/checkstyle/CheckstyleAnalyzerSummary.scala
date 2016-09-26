package com.kstreee.ci.analyzer.checkstyle

import com.kstreee.ci.analyzer.AnalyzerSummary

case class CheckstyleAnalyzerSummary(override val items: List[CheckstyleAnalyzerSummaryItem]) extends AnalyzerSummary {
  override type T = CheckstyleAnalyzerSummaryItem
}