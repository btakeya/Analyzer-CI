package com.kstreee.ci.reporter

import com.kstreee.ci.analysis.{AnalysisConfig, AnalysisReport}
import com.kstreee.ci.reporter.cli.json.{CLIJsonReporter, CLIJsonReporterConfig}
import com.kstreee.ci.reporter.cli.plain.{CLIPlainReporter, CLIPlainReporterConfig}
import com.kstreee.ci.reporter.github.issue.{GitHubIssueReporter, GitHubIssueReporterConfig}
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait Reporter {
  type T <: ReporterConfig
  def report(reporterConfig: T, analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[Unit]]
}

object Reporter {
  private val logger = Logger[this.type]

  type T = ReporterConfig

  def report(reportConfig: T, analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    reportConfig match {
      case (c: CLIJsonReporterConfig) => CLIJsonReporter.report(c, analysisReport)
      case (c: CLIPlainReporterConfig) => CLIPlainReporter.report(c, analysisReport)
      case (c: GitHubIssueReporterConfig) => GitHubIssueReporter.report(c, analysisReport)
      case _ =>
        logger.error(s"Not Implemented, $reportConfig")
        Future(None)
    }
  }

  def report(analysisReport: AnalysisReport)(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[Unit]] = {
    (for {
      reportConfig <- optionT(lift(Try(analysisConfig.reporterConfig)))
      result <- optionT(report(reportConfig, analysisReport))
    } yield result).run
  }
}