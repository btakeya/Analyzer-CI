package com.kstreee.ci.sourcecode.unloader.fs

import java.nio.file.{Files, Path, Paths}

import com.kstreee.ci.util._
import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloader
import com.typesafe.scalalogging.Logger

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class FileSystemSourcecodeUnloader(config: FileSystemSourcecodeUnloaderConfig) extends SourcecodeUnloader {
  private val logger: Logger = Logger[this.type]

  override def unload(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    Future(deleteDirectory(Paths.get(config.path)))
  }

  private def deleteDirectory(path: Path): Option[Unit] = {
    if (!Files.exists(path)) {
      Some(())
    } else if (Files.isDirectory(path)) {
      Files.list(path).iterator().asScala.foldLeft[Option[Unit]](Some(())) { (acc, x) => acc.flatMap(_ => deleteDirectory(x)) }
    } else {
      asOption(Try(Files.delete(path)), (th: Throwable) => logger.error(s"Failed to delete file $path", th))
    }
  }
}