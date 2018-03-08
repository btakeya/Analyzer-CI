package com.kstreee.ci.reporter.cli.plain

import com.kstreee.ci.analysis.AnalysisReport
import com.kstreee.ci.reporter.Reporter

import scala.concurrent.{ExecutionContext, Future}

object CLIPlainReporter extends Reporter {
  override type T = CLIPlainReporterConfig

  override def report(reporterConfig: T, analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    println(
      s"""
         |===============analysis result===============
         |$analysisReport
         |=============================================
       """.stripMargin
    )
    Future(Some(()))
  }
}