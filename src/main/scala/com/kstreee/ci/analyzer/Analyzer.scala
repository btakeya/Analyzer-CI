package com.kstreee.ci.analyzer

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.{AnalysisConfig, AnalysisReport}
import com.kstreee.ci.analyzer.checkstyle.{CheckstyleAnalyzer, CheckstyleAnalyzerConfig, CheckstyleAnalyzerSummary}
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzer, PylintAnalyzerConfig, PylintAnalyzerSummary}
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait Analyzer {
  type T <: AnalyzerConfig
  type U <: AnalyzerSummary
  def buildCmd(analyzerConfig: T)(implicit ctx: ExecutionContext): Future[Option[Seq[String]]]
  def parse(analyzerConfig: T, result: String)(implicit ctx: ExecutionContext): Future[Option[U]]
  def normalize(analyzerConfig: T, summary: U)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]]
}

object Analyzer extends Analyzer {
  private val logger = Logger[this.type]

  override type T = AnalyzerConfig
  override type U = AnalyzerSummary

  override def buildCmd(analyzerConfig: T)(implicit ctx: ExecutionContext): Future[Option[Seq[String]]] = {
    analyzerConfig match {
      case (c: PylintAnalyzerConfig) => PylintAnalyzer.buildCmd(c)
      case (c: CheckstyleAnalyzerConfig) => CheckstyleAnalyzer.buildCmd(c)
      case _ =>
        logger.error(s"Not Implemented, $analyzerConfig")
        Future(None)
    }
  }

  def buildCmd(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[Seq[String]]] = {
    (for {
      analyzerConfig <- optionT(lift(Try(analysisConfig.analyzerConfig)))
      r <- optionT(buildCmd(analyzerConfig))
    } yield r).run
  }

  override def parse(analyzerConfig: T, result: String)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    analyzerConfig match {
      case (c: PylintAnalyzerConfig) => PylintAnalyzer.parse(c, result)
      case (c: CheckstyleAnalyzerConfig) => CheckstyleAnalyzer.parse(c, result)
      case _ =>
        logger.error(s"Not Implemented, $analyzerConfig")
        Future(None)
    }
  }

  def parse(result: String)(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[U]] = {
    (for {
      analyzerConfig <- optionT(lift(Try(analysisConfig.analyzerConfig)))
      r <- optionT(parse(analyzerConfig, result))
    } yield r).run
  }

  override def normalize(analyzerConfig: T, summary: U)(implicit ctx: ExecutionContext): Future[Option[AnalysisReport]] = {
    (analyzerConfig, summary) match {
      case (c: PylintAnalyzerConfig, r: PylintAnalyzerSummary) => PylintAnalyzer.normalize(c, r)
      case (c: CheckstyleAnalyzerConfig, r: CheckstyleAnalyzerSummary) => CheckstyleAnalyzer.normalize(c, r)
      case _ =>
        logger.error(s"Not Implemented, $analyzerConfig")
        Future(None)
    }
  }

  def normalize(summary: U)(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[AnalysisReport]] = {
    (for {
      analyzerConfig <- optionT(lift(Try(analysisConfig.analyzerConfig)))
      r <- optionT(normalize(analyzerConfig, summary))
    } yield r).run
  }
}