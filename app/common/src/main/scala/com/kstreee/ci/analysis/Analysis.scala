package com.kstreee.ci.analysis

import com.kstreee.ci.util._
import com.kstreee.ci.analyzer.Analyzer
import com.kstreee.ci.common.ActorUtils
import com.kstreee.ci.coordinator.Coordinator
import com.kstreee.ci.reporter.Reporter
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object Analysis {
  private val logger: Logger = Logger[this.type]

  def analysis(analysisConfig: AnalysisConfig, classLoader: ClassLoader = Thread.currentThread().getContextClassLoader)
              (implicit ctx: ExecutionContext): Future[Option[Unit]] = {
    if (!ActorUtils.initAhcActor(classLoader)) {
      logger.error("Failed to initialize actor.")
      Future(None)
    } else {
      implicit val ac: AnalysisConfig = analysisConfig
      val analysis =
        for {
          plainReport <- optionT(Coordinator.coordinate)
          parsedReport <- optionT(Analyzer.parse(plainReport))
          analysisReport <- optionT(Analyzer.normalize(parsedReport))
          report <- optionT(Reporter.report(analysisReport))
        } yield report
      callWhenFailed(analysis.run)(() => ActorUtils.destroyAhcActor, ctx)
    }
  }
}