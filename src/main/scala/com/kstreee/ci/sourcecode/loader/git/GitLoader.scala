package com.kstreee.ci.sourcecode.loader.git

import java.nio.file.Path

import com.kstreee.ci.sourcecode.loader.SourcecodeLoader

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait GitLoader extends SourcecodeLoader {
  type T <: GitLoaderConfig

  def checkout(gitLoaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Path]]

  override def load(gitLoaderConfig: T)(implicit ctx: ExecutionContext): Future[Option[Path]] = {
    (for {
      _ <- optionT(gitCloneIfNotExists(gitLoaderConfig))
      r <- optionT(checkout(gitLoaderConfig))
    } yield r).run
  }
}