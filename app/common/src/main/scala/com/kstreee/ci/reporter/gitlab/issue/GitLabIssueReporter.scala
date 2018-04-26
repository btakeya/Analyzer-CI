package com.kstreee.ci.reporter.gitlab.issue

import java.net.URLEncoder

import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.common.AhcActorSystem
import com.kstreee.ci.reporter.Reporter
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class GitLabIssueReporter(config: GitLabIssueReporterConfig, ahcActorSystem: AhcActorSystem) extends Reporter {
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
          .withQueryStringParameters("access_token" -> config.accessToken)
          .post(Json.stringify(Json.toJson(comment)))))
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
    def getUrlEncodedPath(namespace: String, projectName: String): String = {
      val plainTextProjectId = s"$namespace/$projectName"
      URLEncoder.encode(plainTextProjectId, "utf-8")
    }

    val urlEncodedProjectId = getUrlEncodedPath(config.owner, config.repoName)
    info(
      Try(
        Seq(config.baseUrl + "/api/v4", // TODO: API base path
          "projects", urlEncodedProjectId, "issues", config.issueNumber, "notes").mkString("/"))
    ).toOption
  }

  private def getIssueComment(report: AnalysisReport)(implicit ctx: ExecutionContext): Option[GitLabIssueComment] = {

    def author(reportItem: AnalysisReportItem): String = {
      if (reportItem.author != null) {
        reportItem.author.map(_.trim).filter(_ != "").map(s => s"by $s").getOrElse("")
      } else ""
    }

    def position(reportItem: AnalysisReportItem): String = s"${reportItem.path}:${reportItem.line}:${reportItem.column}"

    for {
      analysisName <- asOption(report.analyzerConfig.name, (thr: Throwable) => logger.error("Failed to get analysis name", thr))
      analysisItems <- asOption[List[AnalysisReportItem]](
        report.items, (items: List[AnalysisReportItem]) => items.nonEmpty,
        (thr: Throwable) => logger.info("Failed to get items from analysis report", thr))
    } yield {
      val bugs = analysisItems.map { item =>
        if (item == null) ""
        else {
          s"""
             |- [ ] ${Seq(author(item), position(item)).filter(_.trim.length > 0).mkString(" ")}
             |${if (item.message != null) item.message.trim else ""}
           """.stripMargin
        }
      } map(_.trim) filter(_.length > 0) mkString "\n"

      GitLabIssueComment(s"An Analysis Result of $analysisName:\n$bugs")
    }
  }
}
