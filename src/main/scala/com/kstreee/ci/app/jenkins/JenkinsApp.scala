package com.kstreee.ci.app.jenkins

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.app.App

import scala.concurrent.Future

object JenkinsApp extends App {
  override type T = String

  override def parse(args: T): Future[Option[AnalysisConfig]] = {
    throw new NotImplementedError()
  }
}