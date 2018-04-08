package com.kstreee.ci.reporter.github.issue

import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.common.AhcActorSystem
import com.kstreee.ci.reporter.Reporter
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

case class GitHubIssueReporter(config: GitHubIssueReporterConfig, ahcActorSystem: AhcActorSystem) extends Reporter {
  private val logger = Logger[this.type]

  override def report(analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    val wsClient = ahcActorSystem.getWSClient
    val report =
      for {
        client <- optionT(lift(wsClient))
        url <- optionT(lift(getIssueCommentUrl))
        comment <- optionT(lift(getIssueComment(analysisReport)))
        response <- optionT(lift(client
          .url(url)
          .withHttpHeaders("Content-Type" -> "application/json")
          .withQueryStringParameters("access_token" -> config.token)
          .post(Json.stringify(Json.toJson(comment)))
        ))
      } yield {
        logger.info(s"Got a response from GitHub, $config\n$response")
        ()
      }
    val future = report.run
    future.foreach { _ =>
      wsClient.foreach { client =>
        logger.info(s"Cleaning up wsClient")
        client.close()
      }
    }
    future
  }

  private def getIssueCommentUrl(implicit ctx: ExecutionContext): Option[String] = {
    asOption(
      Try(s"${config.githubApiBaseUrl}/repos/${config.owner}/${config.repo}/issues/${config.number}/comments"),
      (th: Throwable) => logger.error(s"Failed to get GitHub url, $config", th))
  }

  private def author(bug: AnalysisReportItem): String = {
    if (bug.author != null) bug.author.map(_.trim).filter(_.trim != "").map(s => s"by $s").getOrElse("")
    else ""
  }

  private def position(bug: AnalysisReportItem): String = {
    val position = s"${bug.path}:${bug.line}:${bug.column}"
    val linkBaseUrl = s"${config.githubBaseUrl}/${config.owner}/${config.repo}/tree"
    val linkPath = s"${bug.path}#L${bug.line}"
    config.commitSha
      .map(commit => s"[$position]($linkBaseUrl/$commit/$linkPath)")
      .orElse(config.branch)
      .map(branch => s"[$position]($linkBaseUrl/$branch/$linkPath)")
      .getOrElse(s"$position")
  }

  private def getIssueComment(analysisReport: AnalysisReport)
                             (implicit ctx: ExecutionContext): Option[GitHubIssueComment] = {
    for {
      analysisName <- asOption(analysisReport.analyzerConfig.name, (th: Throwable) => logger.error(s"Failed to get analysis name", th))
      analysisItems <- asOption[List[AnalysisReportItem]](
        analysisReport.items,
        (items: List[AnalysisReportItem]) => items.nonEmpty,
        (th: Throwable) => logger.info(s"Failed to get items from analysis report.", th))
    } yield {
      val bugs = analysisItems.map { item =>
        if (item == null) ""
        else {
          s"""
             |- [ ] ${List(author(item), position(item)).filter(_.trim != "").mkString(" ")}
             |${if (item.message != null) item.message.trim else ""}
          """.stripMargin
        }
      } map {
        _.trim
      } filter {
        _.trim != ""
      } mkString "\n"
      GitHubIssueComment(s"An Analysis Result of $analysisName.\n$bugs")
    }
  }
}