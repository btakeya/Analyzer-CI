package com.kstreee.ci.app

import java.nio.file.{Files, Paths}

import com.kstreee.ci.analysis.{Analysis, AnalysisConfig}
import com.kstreee.ci.storage.json.AnalysisConfigJsonLoad
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object CLIApp {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val logger: Logger = Logger[this.type]

  private def loadConfig(args: Array[String]): Future[Option[AnalysisConfig]] = {
    if (args == null || args.isEmpty) {
      logger.error("Failed to parse option path")
      Future(None)
    } else if (!Files.isRegularFile(Paths.get(args(0)))) {
      logger.error(s"File not found, ${args(0)}")
      Future(None)
    } else {
      val optionInputStream = Files.newInputStream(Paths.get(args(0)))
      AnalysisConfigJsonLoad.load(Json.parse(optionInputStream))
    }
  }

  def main(args: Array[String]): Unit = {
    val analysis =
      for {
        // parse arguments & configurations
        analysisConfig <- optionT(loadConfig(args))
        // run an analysis and report a result
        result <- optionT(Analysis(analysisConfig).analysis)
      } yield result
    Await.result(analysis.run, Duration.Inf)
  }
}