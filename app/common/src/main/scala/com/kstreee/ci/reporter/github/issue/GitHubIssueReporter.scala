package com.kstreee.ci.reporter.github.issue

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.reporter.Reporter
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json
import play.api.libs.ws.ahc._
import play.api.libs.ws.DefaultBodyWritables._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, SECONDS}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object GitHubIssueReporter extends Reporter {
  private val logger = Logger[this.type]

  override type T = GitHubIssueReporterConfig

  override def report(reporterConfig: T, analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val defaultConfig = AhcWSClientConfigFactory.forConfig()
    val config = defaultConfig.copy(maxRequestRetry = 2, wsClientConfig = defaultConfig.wsClientConfig.copy(idleTimeout = Duration(10, SECONDS)))
    implicit val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient(config)
    implicit val close: () => Unit = { () =>
      logger.info("Cleaning up WSClient resources...")
      wsClient.close()
      val termination = system.terminate()
      termination.foreach(_ => logger.info("Cleaned a WS Client actor resource."))
      termination.failed.foreach(e => logger.error(s"Failed to clean a WS Client actor resource, $e", e))
      ()
    }

    (for {
      url <- optionT(callWhenFailed(getIssueCommentUrl(reporterConfig)))
      comment <- optionT(callWhenFailed(getIssueComment(analysisReport)))
      response <- optionT(callWhenFailed(lift(wsClient
        .url(url)
        .withHttpHeaders("Content-Type" -> "application/json")
        .withQueryStringParameters("access_token" -> reporterConfig.token)
        .post(Json.stringify(Json.toJson(comment)))
      )))
    } yield {
      close()
      logger.info(s"Got a response from GitHub, $reporterConfig\n$response")
      ()
    }).run
  }

  private def getIssueCommentUrl(reporterConfig: T)(implicit ctx: ExecutionContext): Future[Option[String]] = {
    Future(
      info(
        Try(s"${reporterConfig.githubBaseUrl}/repos/${reporterConfig.owner}/${reporterConfig.repo}/issues/${reporterConfig.number}/comments")
      ).toOption
    )
  }

  private def author(bug: AnalysisReportItem): String = {
    if (bug.author != null) bug.author.map(_.trim).filter(_.trim != "").map(s => s"by $s").getOrElse("")
    else ""
  }

  private def position(bug: AnalysisReportItem): String = {
    s"at ${bug.path}:${bug.line}:${bug.column}"
  }

  private def getIssueComment(analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[GitHubIssueComment]] = {
    if (analysisReport == null || analysisReport.items == null || analysisReport.items.isEmpty) {
      Future(None)
    } else {
      val bugs = analysisReport.items.map { item =>
        if (item == null) ""
        else s"- [ ] ${List(author(item), position(item), item.message).filter(_.trim == "").mkString(" ")}"
      } mkString "\n"
      Future(Some(GitHubIssueComment(s"An Analysis Result of ${analysisReport.analyzerConfig.name}.\n$bugs")))
    }
  }
}