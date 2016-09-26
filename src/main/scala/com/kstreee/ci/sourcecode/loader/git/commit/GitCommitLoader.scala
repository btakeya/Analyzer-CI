package com.kstreee.ci.sourcecode.loader.git.commit

import java.io.File
import java.nio.file.{Path, Paths}

import com.kstreee.ci.util._
import com.kstreee.ci.sourcecode.loader.git.GitLoader
import scalaz.OptionT._
import scalaz.std.scalaFuture._

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

object GitCommitLoader extends GitLoader {
  override type T = GitCommitLoaderConfig

  override def checkout(sourcecodeLoaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    implicit val ret: Future[Option[Path]] = Future(sourcecodeLoaderConfig.basePath.map(Paths.get(_)))
    val fetch = Seq("git", "fetch", "--all")
    val checkout = Seq("git", "checkout", s"${sourcecodeLoaderConfig.commitHash}")
    (for {
      path <- optionT(lift(sourcecodeLoaderConfig.basePath))
      _ <- optionT(liftFailedWhenZero(Process(fetch, new File(path)).!))
      r <- optionT(liftFailedWhenZero(Process(checkout, new File(path)).!))
    } yield r).run
  }
}