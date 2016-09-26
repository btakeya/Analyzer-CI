package com.kstreee.ci.sourcecode.unloader

import java.nio.file.Path

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.sourcecode.unloader.fs.{FileSystemSourcecodeUnloader, FileSystemSourcecodeUnloaderConfig}
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait SourcecodeUnloader {
  type T <: SourcecodeUnloaderConfig
  def unload(sourcecodeUnloaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Unit]]
}

object SourcecodeUnloader extends SourcecodeUnloader {
  private val logger = Logger[this.type]

  override type T = SourcecodeUnloaderConfig

  override def unload(sourcecodeUnloaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    logger.info(s"Loading source... $sourcecodeUnloaderConfig")
    sourcecodeUnloaderConfig match {
      case (c: FileSystemSourcecodeUnloaderConfig) => FileSystemSourcecodeUnloader.unload(c)
      case _ =>
        logger.error(s"Not Implemented, $sourcecodeUnloaderConfig")
        Future(None)
    }
  }

  private def empty(implicit ctx: ExecutionContext): Future[Option[Unit]] = Future(Some(()))

  def unload(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[Unit]] = {
    (for {
      sourcecodeUnloaderConfig <- optionT(lift(Try(analysisConfig.sourcecodeLoaderConfig.sourcecodeUnloaderConfig)))
      result <- if (sourcecodeUnloaderConfig.isDefined) optionT(unload(sourcecodeUnloaderConfig.get)) else optionT(empty)
    } yield {
      logger.info(s"Unloaded source : $result")
      result
    }).run
  }
}