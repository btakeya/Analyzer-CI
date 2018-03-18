package com.kstreee.ci.reporter.github.issue

import com.kstreee.ci.reporter.ReporterConfig

case class GitHubIssueReporterConfig(githubBaseUrl: String,
                                     githubApiBaseUrl: String,
                                     owner: String,
                                     repo: String,
                                     number: Int,
                                     token: String,
                                     branch: Option[String] = None,
                                     commitSha: Option[String] = None) extends ReporterConfig {
  override val name: String = "github_issue_reporter"
}