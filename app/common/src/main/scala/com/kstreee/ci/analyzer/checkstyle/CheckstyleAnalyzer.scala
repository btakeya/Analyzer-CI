package com.kstreee.ci.analyzer.checkstyle

import com.kstreee.ci.analysis.AnalysisReport
import com.kstreee.ci.util._
import com.kstreee.ci.analyzer.Analyzer
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}

case class CheckstyleAnalyzer(config: CheckstyleAnalyzerConfig) extends Analyzer {
  private val logger: Logger = Logger[this.type]

  override def buildCmd(implicit ctx: ExecutionContext): Future[Option[Seq[String]]] = {
    Future(asOption(config.analysisCmd, (th: Throwable) => logger.error(s"Failed to get analysis cmd.", th)))
  }

  override def parse(result: String)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]] = {
    Future(None)
  }
}