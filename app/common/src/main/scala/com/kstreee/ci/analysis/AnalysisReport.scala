package com.kstreee.ci.analysis

import com.kstreee.ci.analyzer.AnalyzerConfig

final case class AnalysisReport(analyzerConfig: AnalyzerConfig, items: List[AnalysisReportItem]) {
  override def toString: String = {
    s"""
       |------ bugs ------
       |${items.map(_.toString).mkString}
       |------------------
     """.stripMargin
  }
}