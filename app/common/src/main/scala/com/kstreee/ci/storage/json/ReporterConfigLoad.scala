package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.reporter.ReporterConfig
import com.kstreee.ci.reporter.cli.json.CLIJsonReporterConfig
import com.kstreee.ci.reporter.cli.plain.CLIPlainReporterConfig
import com.kstreee.ci.reporter.github.issue.GitHubIssueReporterConfig
import com.kstreee.ci.storage.ConfigLoad
import com.typesafe.scalalogging.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}

object ReporterConfigLoad extends ConfigLoad {
  private val logger = Logger[this.type]

  override type U = ReporterConfig
  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    val config =
      for {
        name <- (JsPath \ "name").read[String].reads(data)
        config <- loadConfigByName(name, data)
      } yield config
    Future(asOption(config, (e: JsError) => logger.error("Failed to parse config,", e)))
  }

  private[json] val cliPlainName: String = "cli_plain"
  private[json] implicit val cliPlainReads: Reads[ReporterConfig] = (_: JsValue) => JsSuccess(CLIPlainReporterConfig())
  private[json] val cliJsonName: String = "cli_json"
  private[json] implicit val cliJsonReads: Reads[ReporterConfig] = (_: JsValue) => JsSuccess(CLIJsonReporterConfig())
  private[json] val githubIssueName: String = "github_issue_reporter"
  private[json] implicit val githubIssueReads: Reads[ReporterConfig] = (
    (JsPath \ "github_base_url").read[String] and
      (JsPath \ "github_api_base_url").read[String] and
      (JsPath \ "owner").read[String] and
      (JsPath \ "repo").read[String] and
      (JsPath \ "number").read[Int] and
      (JsPath \ "token").read[String] and
      (JsPath \ "branch").readNullable[String] and
      (JsPath \ "commitSha").readNullable[String]
    ) (GitHubIssueReporterConfig.apply _)

  private[json] def loadConfigByName(name: String, data: T): JsResult[U] = {
    name match {
      case _ if cliPlainName.equalsIgnoreCase(name) => cliPlainReads.reads(data)
      case _ if cliJsonName.equalsIgnoreCase(name) => cliJsonReads.reads(data)
      case _ if githubIssueName.equalsIgnoreCase(name) => githubIssueReads.reads(data)
      case _ =>
        logger.warn(s"Not implemented, $name")
        JsError(s"Not implemented, $name")
    }
  }
}