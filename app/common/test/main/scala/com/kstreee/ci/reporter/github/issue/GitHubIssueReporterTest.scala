package com.kstreee.ci.reporter.github.issue

import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.analyzer.pylint.PylintAnalyzerConfig
import com.kstreee.ci.common.ActorUtilsTestContext
import com.kstreee.ci.reporter.Reporter
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

class GitHubIssueReporterTest(implicit ee: ExecutionEnv) extends Specification {
  "reporter" should {
    "work http client with custom akka config" in new ActorUtilsTestContext() {
      val reporterConfig = GitHubIssueReporterConfig(
        "http://localhost:10201",
        "http://localhost:10201",
        "owner",
        "repo",
        0,
        "token",
        None,
        None
      )

      val analyzerConfig = PylintAnalyzerConfig(List())
      val analysisReportItem = AnalysisReportItem(
        "path",
        "filename",
        0,
        0,
        "test",
        None)
      val analysisReportItems = List(analysisReportItem)
      val analysisReport = AnalysisReport(analyzerConfig, analysisReportItems)

      (for {
        result <- Reporter.report(reporterConfig, analysisReport)
      } yield {
        result must beNone
      }).await
    }
  }
}