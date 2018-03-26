package com.kstreee.ci.sourcecode.loader.git.branch

import java.io.File
import java.nio.file.{Path, Paths}

import com.kstreee.ci.sourcecode.loader.git.GitLoader
import com.kstreee.ci.util._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

case class GitBranchLoader(config: GitBranchLoaderConfig) extends GitLoader(config) {
  override def checkout(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    implicit val ret: Future[Option[Path]] = Future(config.basePath.map(Paths.get(_)))
    val fetch = Seq("git", "fetch", "--all")
    val checkout = Seq("git", "checkout", s"${config.branch}")
    val load =
      for {
        path <- optionT(lift(config.basePath))
        _ <- optionT(liftFailedWhenZero(Process(fetch, new File(path)).!))
        r <- optionT(liftFailedWhenZero(Process(checkout, new File(path)).!))
      } yield r
    load.run
  }
}