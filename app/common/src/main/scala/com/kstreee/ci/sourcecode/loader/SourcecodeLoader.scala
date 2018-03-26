package com.kstreee.ci.sourcecode.loader

import java.nio.file.Path

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.sourcecode.loader.fs.{FileSystemSourcecodeLoader, FileSystemSourcecodeLoaderConfig}
import com.kstreee.ci.sourcecode.loader.git.branch.{GitBranchLoader, GitBranchLoaderConfig}
import com.kstreee.ci.sourcecode.loader.git.commit.{GitCommitLoader, GitCommitLoaderConfig}
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}

trait SourcecodeLoader {
  def load(implicit ctx: ExecutionContext): Future[Option[Path]]
}

object SourcecodeLoader {
  private val logger = Logger[this.type]

  def apply(config: SourcecodeLoaderConfig): Option[SourcecodeLoader] = {
    config match {
      case (c: FileSystemSourcecodeLoaderConfig) => Some(FileSystemSourcecodeLoader(c))
      case (c: GitCommitLoaderConfig) => Some(GitCommitLoader(c))
      case (c: GitBranchLoaderConfig) => Some(GitBranchLoader(c))
      case _ =>
        logger.error(s"Not Implemented, $config")
        None
    }
  }

  def apply(analysisConfig: AnalysisConfig): Option[SourcecodeLoader] =
    asOption(analysisConfig.sourcecodeLoaderConfig, (th: Throwable) => logger.info(s"Failed to get sourcecode loader config.", th))
      .flatMap(config => apply(config))
}