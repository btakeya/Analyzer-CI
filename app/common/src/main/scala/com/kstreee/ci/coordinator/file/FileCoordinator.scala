package com.kstreee.ci.coordinator.file

import java.nio.file.{Files, Paths}

import com.kstreee.ci.util._
import com.kstreee.ci.coordinator.Coordinator
import com.kstreee.ci.sourcecode.loader.SourcecodeLoader
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

case class FileCoordinator(config: FileCoordinatorConfig,
                           loader: SourcecodeLoader) extends Coordinator {
  private val logger = Logger[this.type]

  override def coordinate(implicit ctx: ExecutionContext): Future[Option[String]] = {
    val analysisResult =
      for {
        sourcecodePath <- optionT(loader.load)
        report <- optionT(lift(readReportByFile(sourcecodePath.toString)))
      } yield report

    val future = analysisResult.run
    future
  }

  private[file] def readReportByFile(basePath: String): Option[String] = {
    for {
      path <- asOption(Paths.get(basePath, config.reportPath), (th: Throwable) => logger.error(s"Failed to get report path.", th))
      report <- asOption(Try(String.join("\n", Files.readAllLines(path))), (th: Throwable) => logger.error(s"Failed to read file, ${path.toString}", th))
    } yield report
  }
}