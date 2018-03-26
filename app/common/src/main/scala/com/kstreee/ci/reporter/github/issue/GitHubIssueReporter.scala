package com.kstreee.ci.reporter.github.issue

import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.common.AhcActorSystem
import com.kstreee.ci.reporter.Reporter
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables._

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
        url <- optionT(getIssueCommentUrl)
        comment <- optionT(getIssueComment(analysisReport))
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
    future.foreach(_ => wsClient.foreach(_.close))
    future
  }

  private def getIssueCommentUrl(implicit ctx: ExecutionContext): Future[Option[String]] = {
    Future(
      info(
        Try(s"${config.githubApiBaseUrl}/repos/${config.owner}/${config.repo}/issues/${config.number}/comments")
      ).toOption
    )
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
                             (implicit ctx: ExecutionContext): Future[Option[GitHubIssueComment]] = {
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