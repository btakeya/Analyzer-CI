package com.kstreee.ci.sourcecode.loader

import java.nio.file.Path

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.sourcecode.loader.fs.{FileSystemSourcecodeLoader, FileSystemSourcecodeLoaderConfig}
import com.kstreee.ci.sourcecode.loader.git.branch.{GitBranchLoader, GitBranchLoaderConfig}
import com.kstreee.ci.sourcecode.loader.git.commit.{GitCommitLoader, GitCommitLoaderConfig}
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait SourcecodeLoader {
  type T <: SourcecodeLoaderConfig
  def load(sourcecodeConfig: T)(implicit ctx: ExecutionContext): Future[Option[Path]]
}

object SourcecodeLoader extends SourcecodeLoader {
  private val logger = Logger[this.type]

  override type T = SourcecodeLoaderConfig

  override def load(sourcecodeLoaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    logger.info(s"Loading source... $sourcecodeLoaderConfig")
    sourcecodeLoaderConfig match {
      case (c: FileSystemSourcecodeLoaderConfig) => FileSystemSourcecodeLoader.load(c)
      case (c: GitCommitLoaderConfig) => GitCommitLoader.load(c)
      case (c: GitBranchLoaderConfig) => GitBranchLoader.load(c)
      case _ =>
        logger.error(s"Not Implemented, $sourcecodeLoaderConfig")
        Future(None)
    }
  }

  def load(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[Path]] = {
    (for {
      sourcecodeLoaderConfig <- optionT(lift(Try(analysisConfig.sourcecodeLoaderConfig)))
      result <- optionT(load(sourcecodeLoaderConfig))
    } yield {
      logger.info(s"Loaded source : $result")
      result
    }).run
  }
}