package com.kstreee.ci.coordinator

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.coordinator.cli.{CLICoordinator, CLICoordinatorConfig}
import com.kstreee.ci.util._
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait Coordinator {
  type T <: CoordinatorConfig
  def coordinate(coordinatorConfig: T)(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[String]]
}

object Coordinator extends Coordinator {
  private val logger = Logger[this.type]

  override type T = CoordinatorConfig

  override def coordinate(coordinatorConfig: T)(implicit analysisConfig: AnalysisConfig, executionContext: ExecutionContext): Future[Option[String]] = {
    coordinatorConfig match {
      case (c: CLICoordinatorConfig) => CLICoordinator.coordinate(c)
      case _ =>
        logger.error(s"Not Implemented, $coordinatorConfig")
        Future(None)
    }
  }

  def coordinate(implicit analysisConfig: AnalysisConfig, ctx: ExecutionContext): Future[Option[String]] = {
    (for {
      coordinateConfig <- optionT(lift(Try(analysisConfig.coordinatorConfig)))
      result <- optionT(coordinate(coordinateConfig))
    } yield result).run
  }
}