package com.kstreee.ci.analyzer.checkstyle

import com.kstreee.ci.analysis.AnalysisReport
import com.kstreee.ci.util._
import com.kstreee.ci.analyzer.Analyzer

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._

case class CheckstyleAnalyzer(analyzerConfig: CheckstyleAnalyzerConfig) extends Analyzer {
  override def buildCmd(implicit ctx: ExecutionContext): Future[Option[Seq[String]]] = {
    optionT(lift(analyzerConfig.analysisCmd)).run
  }

  override def parse(result: String)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]] = {
    Future(None)
  }

  private[checkstyle] def normalize(summary: CheckstyleAnalyzerSummary)(implicit ctx: ExecutionContext): Option[AnalysisReport] = {
    None
  }
}