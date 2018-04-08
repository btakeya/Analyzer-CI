package com.kstreee.ci.storage.yaml

import com.kstreee.ci.reporter.ReporterConfig
import com.kstreee.ci.reporter.cli.json.CLIJsonReporterConfig
import com.kstreee.ci.reporter.cli.plain.CLIPlainReporterConfig
import com.kstreee.ci.reporter.github.issue.GitHubIssueReporterConfig
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import net.jcazevedo.moultingyaml._

class ReporterConfigYamlLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "cli json reporter reads" should {
    "valid cli json reporter yaml" in {
      val name = ReporterConfigYamlLoad.cliJsonName
      val yaml = s"""
                    |name: $name
      """.stripMargin
      (for {
        result <- ReporterConfigYamlLoad.load(yaml.parseYaml)
      } yield {
        result must beSome[ReporterConfig]
        result.get must anInstanceOf[CLIJsonReporterConfig]
      }).await
    }
  }

  "cli plain reporter reads" should {
    "valid cli plain reporter yaml" in {
      val name = ReporterConfigYamlLoad.cliPlainName
      val yaml = s"""
                    |name: $name
      """.stripMargin
      (for {
        result <- ReporterConfigYamlLoad.load(yaml.parseYaml)
      } yield {
        result must beSome[ReporterConfig]
        result.get must anInstanceOf[CLIPlainReporterConfig]
      }).await
    }
  }

  "github issue reporter reads" should {
    "valid github issue reporter yaml" in {
      val name = ReporterConfigYamlLoad.githubIssueName
      val githubBaseUrl = "TEST_BASE_URL"
      val githubApiBaseUrl = "TEST_BASE_API_URL"
      val owner = "TEST_OWENR"
      val repo = "TEST_REPO"
      val number = 8
      val token = "TEST_TOKEN"
      val yaml = s"""
                    |name: $name
                    |github_base_url: $githubBaseUrl
                    |github_api_base_url: $githubApiBaseUrl
                    |owner: $owner
                    |repo: $repo
                    |number: $number
                    |token: $token
      """.stripMargin
      (for {
        result <- ReporterConfigYamlLoad.load(yaml.parseYaml)
      } yield {
        result must beSome[ReporterConfig]
        result.get must anInstanceOf[GitHubIssueReporterConfig]
        result.get.asInstanceOf[GitHubIssueReporterConfig].githubBaseUrl mustEqual githubBaseUrl
        result.get.asInstanceOf[GitHubIssueReporterConfig].githubApiBaseUrl mustEqual githubApiBaseUrl
        result.get.asInstanceOf[GitHubIssueReporterConfig].owner mustEqual owner
        result.get.asInstanceOf[GitHubIssueReporterConfig].repo mustEqual repo
        result.get.asInstanceOf[GitHubIssueReporterConfig].number mustEqual number
        result.get.asInstanceOf[GitHubIssueReporterConfig].token mustEqual token
      }).await
    }
  }
}