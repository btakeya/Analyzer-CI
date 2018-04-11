package com.kstreee.ci.storage.yaml

import com.kstreee.ci.util._
import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig
import com.kstreee.ci.sourcecode.loader.fs.FileSystemSourcecodeLoaderConfig
import com.kstreee.ci.sourcecode.loader.git.branch.GitBranchLoaderConfig
import com.kstreee.ci.sourcecode.loader.git.commit.GitCommitLoaderConfig
import com.typesafe.scalalogging.Logger
import net.jcazevedo.moultingyaml._

import scala.concurrent.{ExecutionContext, Future}

object SourcecodeLoaderConfigYamlLoad extends ConfigYamlLoad {
  private val logger: Logger = Logger[this.type]

  override type U = SourcecodeLoaderConfig

  // To support Java
  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = super.load(data)
  override def loadByString(data: String)(implicit ctx: ExecutionContext): Future[Option[U]] = super.loadByString(data)

  private[yaml] val fileSystemName: String = "file_system"
  private[yaml] def fileSystemReads(data: T): Option[SourcecodeLoaderConfig] = {
    data.asYamlObject.getFields(YamlString("source_path")) match {
      case Seq(YamlString(sourcePath)) => Some(FileSystemSourcecodeLoaderConfig(sourcePath))
      case _ =>
        logger.error(s"Failed to parse config for file system, $data")
        None
    }
  }

  private[yaml] val gitCommitName: String = "git_commit"
  private[yaml] def gitCommitReads(data: T): Option[SourcecodeLoaderConfig] = {
    data.asYamlObject.getFields(YamlString("uri"), YamlString("commit_hash")) match {
      case Seq(YamlString(uri), YamlString(commitHash)) =>
        val sourcePath =
          yamlValueByKey(data, YamlString("source_path"), (_: Throwable) => ())
            .flatMap(value => yamlToString(value, (th: Throwable) => logger.info(s"Failed to get source_path from git_branch config, $value}", th)))
        Some(GitCommitLoaderConfig(uri, sourcePath, commitHash))
      case _ =>
        logger.error(s"Failed to parse config for file system, $data")
        None
    }
  }
  private[yaml] val gitBranchName: String = "git_branch"
  private[yaml] def gitBranchReads(data: T): Option[SourcecodeLoaderConfig] = {
    data.asYamlObject.getFields(YamlString("uri"), YamlString("branch")) match {
      case Seq(YamlString(uri), YamlString(branch)) =>
        val sourcePath =
          yamlValueByKey(data, YamlString("source_path"), (_: Throwable) => ())
            .flatMap(value => yamlToString(value, (th: Throwable) => logger.info(s"Failed to get source_path from git_branch config, $value}", th)))
        Some(GitBranchLoaderConfig(uri, sourcePath, branch))
      case _ =>
        logger.error(s"Failed to parse config for file system, $data")
        None
    }
  }

  def loadConfigByName(name: String, data: T): Option[U] = {
    name match {
      case _ if fileSystemName.equalsIgnoreCase(name) => fileSystemReads(data)
      case _ if gitCommitName.equalsIgnoreCase(name) => gitCommitReads(data)
      case _ if gitBranchName.equalsIgnoreCase(name) => gitBranchReads(data)
      case _ =>
        logger.warn(s"Not implemented, $name")
        None
    }
  }
}