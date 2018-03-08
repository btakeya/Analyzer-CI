package com.kstreee.ci.app

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.analyzer.Analyzer
import com.kstreee.ci.coordinator.Coordinator
import com.kstreee.ci.reporter.Reporter

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait App {
  type T

  def loadConfig(args: T): Future[Option[AnalysisConfig]]

  def analysis(args: T)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    (for {
      // parse arguments & configurations
      analysisConfig <- optionT(loadConfig(args))
      // run an analysis and report a result
      result <- optionT(analysis(analysisConfig, ctx))
    } yield result).run
  }

  private def analysis(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[Unit]] = {
    (for {
      plainReport <- optionT(Coordinator.coordinate)
      parsedReport <- optionT(Analyzer.parse(plainReport))
      analysisReport <- optionT(Analyzer.normalize(parsedReport))
      report <- optionT(Reporter.report(analysisReport))
    } yield report).run
  }
}