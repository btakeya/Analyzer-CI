package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig
import com.kstreee.ci.sourcecode.loader.fs.FileSystemSourcecodeLoaderConfig
import com.kstreee.ci.sourcecode.loader.git.branch.GitBranchLoaderConfig
import com.kstreee.ci.sourcecode.loader.git.commit.GitCommitLoaderConfig
import com.kstreee.ci.storage.ConfigLoad
import play.api.libs.json.{JsPath, JsResult, JsValue, Reads}
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}

object SourcecodeLoaderConfigLoad extends ConfigLoad {
  override type T = JsValue
  override type U = SourcecodeLoaderConfig
  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    val config =
      for {
        name <- (JsPath \ "name").read[String].reads(data)
        config <- loadConfigByName(name, data)
      } yield config
    Future(traceJsResult(config).asOpt)
  }

  private[json] val fileSystemName: String = "file_system"
  private[json]val fileSystemReads: Reads[SourcecodeLoaderConfig] =
    (JsPath \ "source_path").read[String].map(FileSystemSourcecodeLoaderConfig(_))
  private[json]val gitCommitName: String = "git_commit"
  private[json]val gitCommitReads: Reads[SourcecodeLoaderConfig] = (
    (JsPath \ "uri").read[String] and
      (JsPath \ "source_path").readNullable[String] and
      (JsPath \ "commit_hash").read[String]
    ) (GitCommitLoaderConfig.apply _)
  private[json]val gitBranchName: String = "git_branch"
  private[json]val gitBranchReads: Reads[SourcecodeLoaderConfig] = (
    (JsPath \ "uri").read[String] and
      (JsPath \ "source_path").readNullable[String] and
      (JsPath \ "branch").read[String]
    ) (GitBranchLoaderConfig.apply _)

  private[json]def loadConfigByName(name: String, data: T): JsResult[U] = {
    name match {
      case _ if fileSystemName.equalsIgnoreCase(name) => fileSystemReads.reads(data)
      case _ if gitCommitName.equalsIgnoreCase(name) => gitCommitReads.reads(data)
      case _ if gitBranchName.equalsIgnoreCase(name) => gitBranchReads.reads(data)
      case _ => throw new NotImplementedError(s"Not implemented, $name")
    }
  }
}