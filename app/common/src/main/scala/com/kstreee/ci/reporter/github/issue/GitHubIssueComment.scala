package com.kstreee.ci.reporter.github.issue

import play.api.libs.json.{JsPath, Writes}
import play.api.libs.functional.syntax._

case class GitHubIssueComment(body: String)

object GitHubIssueComment {
  implicit val gitHubIssueCommentWrites: Writes[GitHubIssueComment] =
    (JsPath \ "body").write[String].contramap(_.body)
}