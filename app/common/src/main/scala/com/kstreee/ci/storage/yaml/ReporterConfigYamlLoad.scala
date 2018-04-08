package com.kstreee.ci.storage.yaml

import com.kstreee.ci.util._
import com.kstreee.ci.reporter.ReporterConfig
import com.kstreee.ci.reporter.cli.json.CLIJsonReporterConfig
import com.kstreee.ci.reporter.cli.plain.CLIPlainReporterConfig
import com.kstreee.ci.reporter.github.issue.GitHubIssueReporterConfig
import com.typesafe.scalalogging.Logger
import net.jcazevedo.moultingyaml._

import scala.concurrent.{ExecutionContext, Future}

object ReporterConfigYamlLoad extends ConfigYamlLoad {
  private val logger: Logger = Logger[this.type]

  override type U = ReporterConfig

  // To support Java
  override def loadByString(data: String)(implicit ctx: ExecutionContext): Future[Option[U]] = super.loadByString(data)

  private[yaml] val cliPlainName: String = "cli_plain"
  private[yaml] def cliPlainReads(data: T): Option[ReporterConfig] =
    Some(CLIPlainReporterConfig())
  private[yaml] val cliJsonName: String = "cli_json"
  private[yaml] def cliJsonReads(data: T): Option[ReporterConfig] =
    Some(CLIJsonReporterConfig())
  private[yaml] val githubIssueName: String = "github_issue_reporter"
  private[yaml] def githubIssueReads(data: T): Option[ReporterConfig] = {
    data.asYamlObject.getFields(
      YamlString("github_base_url"),
      YamlString("github_api_base_url"),
      YamlString("owner"),
      YamlString("repo"),
      YamlString("number"),
      YamlString("token")) match {
      case Seq(
      YamlString(githubBaseUrl),
      YamlString(githubApiBaseUrl),
      YamlString(owner),
      YamlString(repo),
      YamlNumber(number),
      YamlString(token)) =>
        val branch =
          yamlValueByKey(data, YamlString("branch"), (_: Throwable) => ())
            .flatMap(value => yamlToString(value, (th: Throwable) => logger.info(s"Failed to get source_path from git_branch config, $value}", th)))
        val commitSha =
          yamlValueByKey(data, YamlString("commit_sha"), (_: Throwable) => ())
            .flatMap(value => yamlToString(value, (th: Throwable) => logger.info(s"Failed to get source_path from git_branch config, $value}", th)))

        Some(GitHubIssueReporterConfig(
          githubBaseUrl,
          githubApiBaseUrl,
          owner,
          repo,
          number.toInt,
          token,
          branch,
          commitSha))
      case _ =>
        logger.error(s"Failed to parse config for github issue reporter, $data")
        None
    }
  }

  def loadConfigByName(name: String, data: T): Option[U] = {
    name match {
      case _ if cliPlainName.equalsIgnoreCase(name) => cliPlainReads(data)
      case _ if cliJsonName.equalsIgnoreCase(name) => cliJsonReads(data)
      case _ if githubIssueName.equalsIgnoreCase(name) => githubIssueReads(data)
      case _ =>
        logger.warn(s"Not implemented, $name")
        None
    }
  }
}