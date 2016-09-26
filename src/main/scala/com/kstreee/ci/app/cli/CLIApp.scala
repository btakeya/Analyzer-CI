package com.kstreee.ci.app.cli

import java.nio.file.{Files, Paths}

import com.kstreee.ci.app.App
import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.storage.json.AnalysisConfigLoad
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object CLIApp extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val logger = Logger[this.type]

  override type T = Array[String]

  override def parse(args: T): Future[Option[AnalysisConfig]] = {
    if (args == null || args.isEmpty) {
      logger.error("Failed to parse option path")
      Future(None)
    } else if (!Files.isRegularFile(Paths.get(args(0)))) {
      logger.error(s"File not found, ${args(0)}")
      Future(None)
    } else {
      val optionInputStream = Files.newInputStream(Paths.get(args(0)))
      AnalysisConfigLoad.load(Json.parse(optionInputStream))
    }
  }

  def main(args: T): Unit = Await.result(analysis(args), Duration.Inf)
}