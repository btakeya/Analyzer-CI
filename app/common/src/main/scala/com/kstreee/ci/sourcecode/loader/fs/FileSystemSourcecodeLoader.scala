package com.kstreee.ci.sourcecode.loader.fs

import java.nio.file.{Files, Path, Paths}

import com.kstreee.ci.sourcecode.loader.SourcecodeLoader
import com.kstreee.ci.util._

import scala.concurrent.{ExecutionContext, Future}

object FileSystemSourcecodeLoader extends SourcecodeLoader {
  override type T = FileSystemSourcecodeLoaderConfig

  override def load(sourcecodeloaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    lift(Paths.get(sourcecodeloaderConfig.sourcePath))
  }
}