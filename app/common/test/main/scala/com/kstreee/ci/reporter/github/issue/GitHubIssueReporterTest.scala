package com.kstreee.ci.reporter.github.issue

import java.net.ConnectException

import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzerConfig, PylintAnalyzerReportFormat}
import com.kstreee.ci.common.{AhcActorSystem, AhcActorSystemTestContext}
import com.kstreee.ci.reporter.Reporter
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

class GitHubIssueReporterTest(implicit ee: ExecutionEnv) extends Specification {
  implicit val ahcActorSystem: AhcActorSystem = AhcActorSystem()
  "reporter" should {
    "work http client with custom akka config" in new AhcActorSystemTestContext(ahcActorSystem) {
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

      val analyzerConfig = PylintAnalyzerConfig(List(), PylintAnalyzerReportFormat.Json)
      val analysisReportItem = AnalysisReportItem(
        "path",
        "filename",
        0,
        0,
        "test",
        None)
      val analysisReportItems = List(analysisReportItem)
      val analysisReport = AnalysisReport(analyzerConfig, analysisReportItems)

      val reporter = Reporter(reporterConfig, Some(ahcActorSystem))
      reporter must beSome[Reporter]
      reporter.get must anInstanceOf[GitHubIssueReporter]
      reporter.get.report(analysisReport).failed.map { e =>
        e must anInstanceOf[ConnectException]
        e.getMessage mustEqual "Connection refused: localhost/0:0:0:0:0:0:0:1:10201"
      }.await
    }
  }
}