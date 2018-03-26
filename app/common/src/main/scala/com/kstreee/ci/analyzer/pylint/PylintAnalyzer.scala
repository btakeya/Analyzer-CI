package com.kstreee.ci.analyzer.pylint

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.{AnalysisReport, AnalysisReportItem}
import com.kstreee.ci.analyzer.Analyzer
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class PylintAnalyzer(analyzerConfig: PylintAnalyzerConfig) extends Analyzer {
  private val logger = Logger[this.type]

  override def buildCmd(implicit ctx: ExecutionContext): Future[Option[Seq[String]]] = {
    val cmd = asOption(analyzerConfig.analysisCmd, (th: Throwable) => logger.error(s"Failed to get analysis command", th))
      .map(cmd => cmd ++ Seq("--output-format=json"))
    lift(cmd)
  }

  override def parse(result: String)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]] = {
    val summary = tap(
      Json.parse(result).asOpt[List[PylintAnalyzerSummaryItem]].map(lst => PylintAnalyzerSummary(lst)),
      logger.error(s"Failed to parse pylint summary"))
    Future(summary.flatMap(s => normalize(s)))
  }

  private def normalize(summary: PylintAnalyzerSummary)(implicit ctx: ExecutionContext): Option[AnalysisReport] = {
    if (summary == null || summary.items == null || summary.items.isEmpty) {
      logger.info(s"A summary is null or has empty items. $analyzerConfig, $summary")
      None
    } else {
      val items = asOption(
        Try(summary.items.map(r => AnalysisReportItem(path = r.path, filename = "", line = r.line, column = r.column, message = r.message, author = None))),
        (th: Throwable) => logger.error(s"Failed to transform summary to analysis report.", th))
      items.map(AnalysisReport(analyzerConfig, _))
    }
  }
}