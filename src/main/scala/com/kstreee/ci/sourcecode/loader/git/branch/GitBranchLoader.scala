package com.kstreee.ci.sourcecode.loader.git.branch

import java.io.File
import java.nio.file.{Path, Paths}

import com.kstreee.ci.sourcecode.loader.git.GitLoader
import com.kstreee.ci.util._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

object GitBranchLoader extends GitLoader {
  override type T = GitBranchLoaderConfig

  override def checkout(sourcecodeLoaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    implicit val ret: Future[Option[Path]] = Future(sourcecodeLoaderConfig.basePath.map(Paths.get(_)))
    val fetch = Seq("git", "fetch", "--all")
    val checkout = Seq("git", "checkout", s"${sourcecodeLoaderConfig.branch}")
    (for {
      path <- optionT(lift(sourcecodeLoaderConfig.basePath))
      _ <- optionT(liftFailedWhenZero(Process(fetch, new File(path)).!))
      r <- optionT(liftFailedWhenZero(Process(checkout, new File(path)).!))
    } yield r).run
  }
}