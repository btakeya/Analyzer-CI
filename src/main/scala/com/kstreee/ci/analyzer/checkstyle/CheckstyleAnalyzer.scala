package com.kstreee.ci.analyzer.checkstyle

import com.kstreee.ci.analysis.AnalysisReport
import com.kstreee.ci.util._
import com.kstreee.ci.analyzer.Analyzer

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object CheckstyleAnalyzer extends Analyzer {
  override type T = CheckstyleAnalyzerConfig
  override type U = CheckstyleAnalyzerSummary

  override def buildCmd(analyzerConfig: T)(implicit ctx: ExecutionContext): Future[Option[Seq[String]]] = {
    (for {
      cmd <- optionT(lift(analyzerConfig.analysisCmd))
    } yield cmd ++ Seq("--output-format=json")).run
  }

  override def parse(analyzerConfig: T, result: String)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    Future(None)
  }

  override def normalize(analyzerConfig: T, summary: U)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]] = {
    Future(None)
  }
}