package com.kstreee.ci.reporter.gitlab.issue

import com.kstreee.ci.reporter.ReporterConfig

case class GitLabIssueReporterConfig(baseUrl: String,
                                     owner: String,
                                     repoName: String,
                                     issueNumber: Int,
                                     accessToken: String,
                                     branch: Option[String] = None,
                                     commitSha: Option[String] = None
                                     ) extends ReporterConfig {
  override val name: String = "gitlab_issue_reporter"
}
