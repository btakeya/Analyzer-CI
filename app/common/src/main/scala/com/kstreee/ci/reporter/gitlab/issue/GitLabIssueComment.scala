package com.kstreee.ci.reporter.gitlab.issue

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class GitLabIssueComment(note: String)

object GitLabIssueComment {
  implicit val gitHubIssueCommentWrites: Writes[GitLabIssueComment] =
    (JsPath \ "body").write[String].contramap(_.note)
}
