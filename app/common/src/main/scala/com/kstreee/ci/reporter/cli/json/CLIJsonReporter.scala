package com.kstreee.ci.reporter.cli.json

import com.kstreee.ci.analysis.AnalysisReport
import com.kstreee.ci.reporter.Reporter

import scala.concurrent.{ExecutionContext, Future}

case class CLIJsonReporter(reporterConfig: CLIJsonReporterConfig) extends Reporter {
  override def report(analysisReport: AnalysisReport)(implicit ctx: ExecutionContext): Future[Option[Unit]] = {
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