package com.kstreee.ci.sourcecode.loader

import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloaderConfig
import com.kstreee.ci.sourcecode.unloader.fs.FileSystemSourcecodeUnloaderConfig
import com.kstreee.ci.util._

import scala.sys.process._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

package object git {
  def gitCloneIfNotExists(gitLoaderConfig: GitLoaderConfig)(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    implicit val basePath: Future[Option[Path]] = Future(gitLoaderConfig.basePath.map(Paths.get(_)))
    if (exists(gitLoaderConfig.sourcePath)) {
      basePath
    } else {
      (for {
        path <- optionT(basePath)
        r <- optionT(liftFailedWhenZero(
          Process(Seq("git", "clone", gitLoaderConfig.uri, path.toString), new File("./")).!
        ))
      } yield r).run
    }
  }

  def createTmpDirectoryIfNotExists(sourcePath: Option[String]): Option[String] = {
    if (exists(sourcePath)) sourcePath else traceTry(Try(Files.createTempDirectory("analysis-ci").toString)).toOption
  }

  def fileSystemUnloadIfNotExists(sourcePath: Option[String], basePath: String): Option[SourcecodeUnloaderConfig] = {
    if (exists(sourcePath)) None else Some(FileSystemSourcecodeUnloaderConfig(basePath))
  }

  private def exists(sourcePath: Option[String]): Boolean = (
    sourcePath.isDefined
      && Files.exists(Paths.get(sourcePath.get))
      && Files.exists(Paths.get(sourcePath.get, ".git"))
    )
}