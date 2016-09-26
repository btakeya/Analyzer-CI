package com.kstreee.ci.sourcecode.unloader.fs

import java.nio.file.{Files, Path, Paths}

import com.kstreee.ci.util._
import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloader

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object FileSystemSourcecodeUnloader extends SourcecodeUnloader {
  type T = FileSystemSourcecodeUnloaderConfig
  def unload(sourcecodeUnloaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    Future(deleteDirectory(Paths.get(sourcecodeUnloaderConfig.path)))
  }

  private def deleteDirectory(path: Path): Option[Unit] = {
    if (!Files.exists(path)) {
      Some(())
    } else if (Files.isDirectory(path)) {
      Files.list(path).iterator().asScala.foldLeft[Option[Unit]](Some(())) { (acc, x) =>
        acc.flatMap(_ => deleteDirectory(x))
      }
    } else {
      traceTry(Try(Files.delete(path))).toOption
    }
  }
}