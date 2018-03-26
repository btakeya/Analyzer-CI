package com.kstreee.ci.coordinator.cli

import java.io.File
import java.nio.file.Path

import com.kstreee.ci.analyzer.Analyzer
import com.kstreee.ci.coordinator.Coordinator
import com.kstreee.ci.sourcecode.loader.SourcecodeLoader
import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloader
import com.typesafe.scalalogging.Logger

import sys.process._
import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

case class CLICoordinator(config: CLICoordinatorConfig,
                          analyzer: Analyzer,
                          loader: SourcecodeLoader,
                          unloader: Option[SourcecodeUnloader]) extends Coordinator {
  private val logger = Logger[this.type]

  override def coordinate(implicit ctx: ExecutionContext): Future[Option[String]] = {
    val analysisResult =
      for {
        sourcecodePath <- optionT(loader.load)
        cmd <- optionT(analyzer.buildCmd)
        analysisResult <- optionT(analysis(sourcecodePath, cmd))
      } yield analysisResult

    val future = analysisResult.run
    future.foreach(_ => unloader.map(_.unload))
    future
  }

  private def analysis(sourcecodePath: Path, cmd: Seq[String])(implicit ctx: ExecutionContext): Future[Option[String]] = {
    val stdout = new StringBuffer
    val stderr = new StringBuffer
    val process = Process(cmd, new File(sourcecodePath.toString))
      .run(ProcessLogger(line => stdout append line append "\n", line => stderr append line append "\n"))
    logger.info(s"Done : ${process.exitValue()}")
    Future(Some(stdout.toString))
  }
}