package com.kstreee.ci.sourcecode.loader.fs

import java.nio.file.{Files, Path, Paths}

import com.kstreee.ci.sourcecode.loader.SourcecodeLoader
import com.kstreee.ci.util._

import scala.concurrent.{ExecutionContext, Future}

case class FileSystemSourcecodeLoader(config: FileSystemSourcecodeLoaderConfig) extends SourcecodeLoader {
  override def load(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    lift(Paths.get(config.sourcePath))
  }
}