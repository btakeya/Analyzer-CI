package com.kstreee.ci.analyzer

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.{AnalysisConfig, AnalysisReport}
import com.kstreee.ci.analyzer.checkstyle.{CheckstyleAnalyzer, CheckstyleAnalyzerConfig}
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzer, PylintAnalyzerConfig}
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}

trait Analyzer {
  def buildCmd(implicit ctx: ExecutionContext): Future[Option[Seq[String]]]
  def parse(result: String)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]]
}

object Analyzer {
  private val logger = Logger[this.type]

  def apply(analysisConfig: AnalysisConfig): Option[Analyzer] =
    asOption(analysisConfig.analyzerConfig, (th: Throwable) => logger.info(s"Failed to get analyzer config.", th)).flatMap(config => apply(config))

  def apply(config: AnalyzerConfig): Option[Analyzer] = {
    config match {
      case (c: PylintAnalyzerConfig) => Some(PylintAnalyzer.apply(c))
      case (c: CheckstyleAnalyzerConfig) => Some(CheckstyleAnalyzer.apply(c))
      case _ =>
        logger.error(s"Not Implemented, $config")
        None
    }
  }
}