package com.kstreee.ci.analyzer.checkstyle

import com.kstreee.ci.analyzer.AnalyzerConfig

case class CheckstyleAnalyzerConfig(override val analysisCmd: Seq[String]) extends AnalyzerConfig {
  override val name: String = "checkstyle"
}