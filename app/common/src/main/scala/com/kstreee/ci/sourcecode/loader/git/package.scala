package com.kstreee.ci.sourcecode.loader

import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloaderConfig
import com.kstreee.ci.sourcecode.unloader.fs.FileSystemSourcecodeUnloaderConfig
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger

import scala.sys.process._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

package object git {
  private val logger: Logger = Logger[this.type]

  def gitCloneIfNotExists(gitLoaderConfig: GitLoaderConfig)(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    val load =
      for {
        basePath <- gitLoaderConfig.basePath
        path <- asOption[Path](
          Paths.get(basePath),
          (p: Path) => exists(p),
          (th: Throwable) => logger.error(s"Failed to get temporary directory.", th))
        _ <- asOption[Int](
          Process(Seq("git", "clone", gitLoaderConfig.uri, path.toString), new File("./")).!,
          (result: Int) => result == 0,
          (th: Throwable) => logger.error(s"Failed to execute git clone.", th))
      } yield path
    Future(load)
  }

  def createTmpDirectoryIfNotExists(sourcePath: Option[String]): Option[String] = {
    if (exists(sourcePath)) sourcePath
    else asOption(Try(Files.createTempDirectory("analysis-ci").toString), (th: Throwable) => logger.error(s"Failed to create temp directory.", th))
  }

  def fileSystemUnloadIfNotExists(sourcePath: Option[String], basePath: String): Option[SourcecodeUnloaderConfig] = {
    if (exists(sourcePath)) None
    else Some(FileSystemSourcecodeUnloaderConfig(basePath))
  }

  private def exists(path: Option[String]): Boolean = path.isDefined && exists(Paths.get(path.get))
  private def exists(path: Path): Boolean = Files.exists(path) && Files.exists(Paths.get(path.toString, ".git"))
}