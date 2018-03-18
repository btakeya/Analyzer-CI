package com.kstreee.ci.reporter.github.issue

import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.common.ActorUtils
import com.kstreee.ci.reporter.Reporter
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object GitHubIssueReporter extends Reporter {
  private val logger = Logger[this.type]

  override type T = GitHubIssueReporterConfig

  private val wsClient: Option[StandaloneAhcWSClient] = ActorUtils.getWSClient

  override def report(reporterConfig: T, analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    implicit val close: () => Unit = { () => if (wsClient.isDefined) wsClient.get.close() else () }

    (for {
      client <- optionT(lift(wsClient))
      url <- optionT(callWhenFailed(getIssueCommentUrl(reporterConfig, ctx)))
      comment <- optionT(callWhenFailed(getIssueComment(analysisReport)(reporterConfig, ctx)))
      response <- optionT(callWhenFailed(lift(client
        .url(url)
        .withHttpHeaders("Content-Type" -> "application/json")
        .withQueryStringParameters("access_token" -> reporterConfig.token)
        .post(Json.stringify(Json.toJson(comment)))
      )))
    } yield {
      close()
      logger.info(s"Got a response from GitHub, $reporterConfig\n$response")
      ()
    }).run.recover {
      case _ => None
    }
  }

  private def getIssueCommentUrl(implicit reporterConfig: T, ctx: ExecutionContext): Future[Option[String]] = {
    Future(
      info(
        Try(s"${reporterConfig.githubApiBaseUrl}/repos/${reporterConfig.owner}/${reporterConfig.repo}/issues/${reporterConfig.number}/comments")
      ).toOption
    )
  }

  private def author(bug: AnalysisReportItem): String = {
    if (bug.author != null) bug.author.map(_.trim).filter(_.trim != "").map(s => s"by $s").getOrElse("")
    else ""
  }

  private def position(bug: AnalysisReportItem)(implicit reporterConfig: T): String = {
    val position = s"${bug.path}:${bug.line}:${bug.column}"
    val linkBaseUrl = s"${reporterConfig.githubBaseUrl}/${reporterConfig.owner}/${reporterConfig.repo}/tree"
    val linkPath = s"${bug.path}#L${bug.line}"
    reporterConfig.commitSha
      .map(commit => s"[$position]($linkBaseUrl/$commit/$linkPath)")
      .orElse(reporterConfig.branch)
      .map(branch => s"[$position]($linkBaseUrl/$branch/$linkPath)")
      .getOrElse(s"$position")
  }

  private def getIssueComment(analysisReport: AnalysisReport)
                             (implicit reporterConfig: T, ctx: ExecutionContext): Future[Option[GitHubIssueComment]] = {
    if (analysisReport == null || analysisReport.items == null || analysisReport.items.isEmpty) {
      Future(None)
    } else {
      val bugs = analysisReport.items.map { item =>
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
      Future(Some(GitHubIssueComment(s"An Analysis Result of ${analysisReport.analyzerConfig.name}.\n$bugs")))
    }
  }
}