package com.kstreee.ci.reporter

import com.kstreee.ci.analysis.{AnalysisConfig, AnalysisReport}
import com.kstreee.ci.common.AhcActorSystem
import com.kstreee.ci.reporter.cli.json.{CLIJsonReporter, CLIJsonReporterConfig}
import com.kstreee.ci.reporter.cli.plain.{CLIPlainReporter, CLIPlainReporterConfig}
import com.kstreee.ci.reporter.github.issue.{GitHubIssueReporter, GitHubIssueReporterConfig}
import com.kstreee.ci.reporter.gitlab.issue.{GitLabIssueReporter, GitLabIssueReporterConfig}
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}

trait Reporter {
  def report(analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[Unit]]
}

object Reporter {
  private val logger: Logger = Logger[this.type]

  def apply(analysisConfig: AnalysisConfig, actor: Option[AhcActorSystem]): Option[Reporter] =
    asOption(
      analysisConfig.reporterConfig,
      (th: Throwable) => logger.info(s"Failed to get reporter config.", th)).flatMap(config => apply(config, actor))

  def apply(config: ReporterConfig, actor: Option[AhcActorSystem]): Option[Reporter] = {
    config match {
      case c: CLIJsonReporterConfig => Some(CLIJsonReporter(c))
      case c: CLIPlainReporterConfig => Some(CLIPlainReporter(c))
      case c: GitHubIssueReporterConfig => tap(actor, logger.error("AhcActorSystem is none. GitHub Issue Reporter requires AhcActorSystem to use ahc."))
        .map(a => GitHubIssueReporter(c, a))
      case c: GitLabIssueReporterConfig => tap(actor, logger.error("AhcActorSystem is missing. GitLab Issue Reporter requires AhcActorSystem to use ahc."))
        .map(a => GitLabIssueReporter(c, a))
      case _ =>
        logger.error(s"Not Implemented, $config")
        None
    }
  }
}