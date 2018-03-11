package com.kstreee.ci.analyzer.pylint

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.analyzer.Analyzer
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object PylintAnalyzer extends Analyzer {
  private val logger = Logger[this.type]

  override type T = PylintAnalyzerConfig
  override type U = PylintAnalyzerSummary

  override def buildCmd(analyzerConfig: T)(implicit ctx: ExecutionContext): Future[Option[Seq[String]]] = {
    (for {
      cmd <- optionT(lift(analyzerConfig.analysisCmd))
    } yield cmd ++ Seq("--output-format=json")).run
  }

  override def parse(analyzerConfig: T, result: String)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    (for {
      items <- optionT(lift(Json.parse(result).asOpt[List[PylintAnalyzerSummaryItem]]))
    } yield {
      PylintAnalyzerSummary(items)
    }).run
  }

  override def normalize(analyzerConfig: T, summary: U)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]] = {
    if (summary == null || summary.items == null || summary.items.isEmpty) {
      logger.info(s"A summary is null or has empty items. $analyzerConfig, $summary")
      Future(None)
    } else {
      (for {
        items <- optionT(lift(Try(summary.items.map(r => AnalysisReportItem(
          path = r.path,
          filename = "",
          line = r.line,
          column = r.column,
          message = r.message,
          author = None
        )))))
      } yield {
        AnalysisReport(analyzerConfig, items)
      }).run
    }
  }
}