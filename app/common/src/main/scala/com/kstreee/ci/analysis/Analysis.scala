package com.kstreee.ci.analysis

import com.kstreee.ci.analyzer.Analyzer
import com.kstreee.ci.coordinator.Coordinator
import com.kstreee.ci.reporter.Reporter

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait Analysis {
  def analysis(analysisConfig: AnalysisConfig)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    implicit val ac: AnalysisConfig = analysisConfig
    (for {
      plainReport <- optionT(Coordinator.coordinate)
      parsedReport <- optionT(Analyzer.parse(plainReport))
      analysisReport <- optionT(Analyzer.normalize(parsedReport))
      report <- optionT(Reporter.report(analysisReport))
    } yield report).run
  }
}