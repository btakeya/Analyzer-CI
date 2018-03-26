package com.kstreee.ci.analysis

import com.kstreee.ci.util._
import com.kstreee.ci.analyzer.Analyzer
import com.kstreee.ci.common.AhcActorSystem
import com.kstreee.ci.coordinator.Coordinator
import com.kstreee.ci.reporter.Reporter

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

case class Analysis(analysisConfig: AnalysisConfig, classLoader: ClassLoader = Thread.currentThread().getContextClassLoader)
                   (implicit ctx: ExecutionContext) {

  def analysis: Future[Option[Unit]] = {
    val ahcActorSystem = AhcActorSystem(classLoader)
    val analysis =
      for {
        coordinator <- optionT(lift(Coordinator(analysisConfig)))
        analyzer <- optionT(lift(Analyzer(analysisConfig)))
        reporter <- optionT(lift(Reporter(analysisConfig, ahcActorSystem.toOption)))
        plainReport <- optionT(coordinator.coordinate)
        analysisReport <- optionT(analyzer.parse(plainReport))
        _ <- optionT(reporter.report(analysisReport))
      } yield {
        ()
      }
    val future = analysis.run
    future.foreach(_ => ahcActorSystem.destroy())
    future
  }
}