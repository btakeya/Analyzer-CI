package com.kstreee.ci.analyzer.checkstyle

import com.kstreee.ci.analysis.AnalysisReport
import com.kstreee.ci.util._
import com.kstreee.ci.analyzer.Analyzer

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

case class CheckstyleAnalyzer(analyzerConfig: CheckstyleAnalyzerConfig) extends Analyzer {
  override def buildCmd(implicit ctx: ExecutionContext): Future[Option[Seq[String]]] = {
    (for {
      cmd <- optionT(lift(analyzerConfig.analysisCmd))
    } yield cmd ++ Seq("--output-format=json")).run
  }

  override def parse(result: String)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]] = {
    Future(None)
  }

  private[checkstyle] def normalize(summary: CheckstyleAnalyzerSummary)(implicit ctx: ExecutionContext): Option[AnalysisReport] = {
    None
  }
}