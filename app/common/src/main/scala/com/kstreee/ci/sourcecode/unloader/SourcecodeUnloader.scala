package com.kstreee.ci.sourcecode.unloader

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.sourcecode.unloader.fs.{FileSystemSourcecodeUnloader, FileSystemSourcecodeUnloaderConfig}
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}

trait SourcecodeUnloader {
  def unload(implicit ctx: ExecutionContext): Future[Option[Unit]]
}

object SourcecodeUnloader {
  private val logger = Logger[this.type]

  def apply(sourcecodeUnloaderConfig: SourcecodeUnloaderConfig): Option[SourcecodeUnloader] = {
    sourcecodeUnloaderConfig match {
      case (c: FileSystemSourcecodeUnloaderConfig) => Some(FileSystemSourcecodeUnloader(c))
      case _ =>
        logger.error(s"Not Implemented, $sourcecodeUnloaderConfig")
        None
    }
  }

  def apply(analysisConfig: AnalysisConfig): Option[SourcecodeUnloader] = {
    asOption(analysisConfig.sourcecodeLoaderConfig, (th: Throwable) => logger.info(s"Failed to get reporter config.", th))
      .flatMap(loaderConfig => loaderConfig.sourcecodeUnloaderConfig
        .flatMap(unloaderConfig => asOption(unloaderConfig, (th: Throwable) => logger.info(s"unlloader config has been defined but null.", th)))
        .flatMap(unloaderConfig => apply(unloaderConfig)))
  }
}