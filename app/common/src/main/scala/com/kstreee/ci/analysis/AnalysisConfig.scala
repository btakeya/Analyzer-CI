package com.kstreee.ci.analysis

import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.coordinator.CoordinatorConfig
import com.kstreee.ci.reporter.ReporterConfig
import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig

final case class AnalysisConfig(analyzerConfig: AnalyzerConfig,
                                coordinatorConfig: CoordinatorConfig,
                                sourcecodeLoaderConfig: SourcecodeLoaderConfig,
                                reporterConfig: ReporterConfig) extends Config {
  override val name: String = "analysis"

  override def toString: String = {
    s"""
       |analyzer config : $analyzerConfig
       |coordinator config : $coordinatorConfig
       |sourcecodeloader config : $sourcecodeLoaderConfig
       |reporter config : $reporterConfig
     """.stripMargin
  }
}