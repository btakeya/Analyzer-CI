package com.kstreee.ci.reporter.github.issue

import com.kstreee.ci.reporter.ReporterConfig

case class GitHubIssueReporterConfig(githubBaseUrl: String,
                                     owner: String,
                                     repo: String,
                                     number: Int,
                                     token: String) extends ReporterConfig {
  override val name: String = "github_issue_reporter"
}