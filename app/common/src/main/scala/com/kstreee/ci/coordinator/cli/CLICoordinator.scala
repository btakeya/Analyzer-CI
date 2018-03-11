package com.kstreee.ci.coordinator.cli

import java.io.File
import java.nio.file.Path

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.analyzer.Analyzer
import com.kstreee.ci.coordinator.Coordinator
import com.kstreee.ci.sourcecode.loader.SourcecodeLoader
import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloader
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration.{Duration, SECONDS}
import sys.process._
import scala.concurrent.{Await, ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object CLICoordinator extends Coordinator {
  private val logger = Logger[this.type]

  override type T = CLICoordinatorConfig

  override def coordinate(coordinatorConfig: T)(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[String]] = {
    val analysisResult =
      (for {
        sourcecodePath <- optionT(SourcecodeLoader.load)
        cmd <- optionT(Analyzer.buildCmd)
        analysisResult <- optionT(analysis(sourcecodePath, cmd))
      } yield analysisResult).run

    // Blocking until an analysis done based.
    val report: Option[String] =
      if (coordinatorConfig.timeoutSeconds <= 0) {
        Await.result(analysisResult, Duration.Inf)
      } else {
        Await.result(analysisResult, Duration(coordinatorConfig.timeoutSeconds, SECONDS))
      }

    (for {
      report <- optionT(lift(report))
      _ <- optionT(SourcecodeUnloader.unload)
    } yield report).run
  }

  private def analysis(sourcecodePath: Path, cmd: Seq[String])(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[String]] = {
    logger.info(
      s"""
         |----------analysis config-----------
         |$analysisConfig
         |------------------------------------
       """.stripMargin
    )
    val stdout = new StringBuffer
    val stderr = new StringBuffer
    val process = Process(cmd, new File(sourcecodePath.toString))
      .run(ProcessLogger(line => stdout append line append "\n", line => stderr append line append "\n"))
    logger.info(s"Done : ${process.exitValue()}")
    Future(Some(stdout.toString))
  }
}