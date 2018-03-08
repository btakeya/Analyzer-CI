package com.kstreee.ci.analyzer

import com.kstreee.ci.analysis.Config

trait AnalyzerConfig extends Config {
  val analysisCmd: Seq[String]
}