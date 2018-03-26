package com.kstreee.ci.sourcecode.loader.git

import java.nio.file.Path

import com.kstreee.ci.sourcecode.loader.SourcecodeLoader

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

abstract class GitLoader(config: GitLoaderConfig) extends SourcecodeLoader {
  def checkout(implicit ctx: ExecutionContext): Future[Option[Path]]
  override def load(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    val load =
      for {
        _ <- optionT(gitCloneIfNotExists(config))
        r <- optionT(checkout)
      } yield r
    load.run
  }
}