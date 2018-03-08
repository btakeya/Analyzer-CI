package com.kstreee.ci.analyzer.pylint

import com.kstreee.ci.analyzer.AnalyzerConfig

case class PylintAnalyzerConfig(override val analysisCmd: Seq[String]) extends AnalyzerConfig {
  override val name: String = "pylint"
}