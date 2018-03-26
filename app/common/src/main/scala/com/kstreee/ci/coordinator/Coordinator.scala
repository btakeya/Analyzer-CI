package com.kstreee.ci.coordinator

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.analyzer.Analyzer
import com.kstreee.ci.coordinator.cli.{CLICoordinator, CLICoordinatorConfig}
import com.kstreee.ci.sourcecode.loader.SourcecodeLoader
import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloader
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}

trait Coordinator {
  def coordinate(implicit ctx: ExecutionContext): Future[Option[String]]
}

object Coordinator {
  private val logger = Logger[this.type]

  def apply(analysisConfig: AnalysisConfig): Option[Coordinator] = {
    for {
      analyzer <- tap(Analyzer(analysisConfig), logger.error(s"Failed to get analyzer."))
      loader <- tap(SourcecodeLoader(analysisConfig), logger.error(s"Failed to get sourcecode loader."))
      config <- asOption(analysisConfig.coordinatorConfig, (th: Throwable) => logger.info(s"Failed to get coordinator config.", th))
      coordinator <- apply(config, analyzer, loader, SourcecodeUnloader(analysisConfig))
    } yield {
      coordinator
    }
  }

  def apply(config: CoordinatorConfig, analyzer: Analyzer, loader: SourcecodeLoader, unloader: Option[SourcecodeUnloader]): Option[Coordinator] = {
    config match {
      case (c: CLICoordinatorConfig) => Some(CLICoordinator(c, analyzer, loader, unloader))
      case _ =>
        logger.error(s"Not Implemented, $config")
        None
    }
  }
}