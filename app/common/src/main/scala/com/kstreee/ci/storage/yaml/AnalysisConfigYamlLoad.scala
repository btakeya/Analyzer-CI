package com.kstreee.ci.storage.yaml

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.coordinator.CoordinatorConfig
import com.kstreee.ci.reporter.ReporterConfig
import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig
import com.kstreee.ci.storage.ConfigLoad
import com.typesafe.scalalogging.Logger
import net.jcazevedo.moultingyaml._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object AnalysisConfigYamlLoad extends ConfigLoad {
  private val logger: Logger = Logger[this.type]

  override type T = YamlValue
  override type U = AnalysisConfig

  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = loadWithDefault(data, None, None, None, None)

  // To support Java
  def loadByString(data: String)(implicit ctx: ExecutionContext): Future[Option[U]] = loadWithDefault(data.parseYaml, None, None, None, None)

  def loadWithDefault(data: T,
                      analyzerConfigDefault: Option[AnalyzerConfig] = None,
                      coordinatorConfigDefault: Option[CoordinatorConfig] = None,
                      sourcecodeLoaderConfigDefault: Option[SourcecodeLoaderConfig] = None,
                      reporterConfigDefault: Option[ReporterConfig] = None)(implicit ctx: ExecutionContext): Future[Option[U]] = {

    val analyzerConfig: Future[Option[AnalyzerConfig]] =
      if (analyzerConfigDefault.isDefined) lift(analyzerConfigDefault)
      else loadAnalyzerConfig(data)

    val coordinatorConfig: Future[Option[CoordinatorConfig]] =
      if (coordinatorConfigDefault.isDefined) lift(coordinatorConfigDefault)
      else loadCoordinatorConfig(data)

    val sourcecodeLoaderConfig: Future[Option[SourcecodeLoaderConfig]] =
      if (sourcecodeLoaderConfigDefault.isDefined) lift(sourcecodeLoaderConfigDefault)
      else loadSourcecodeLoaderConfig(data)

    val reporterConfig: Future[Option[ReporterConfig]] =
      if (reporterConfigDefault.isDefined) lift(reporterConfigDefault)
      else loadReporterConfig(data)

    val analysisConfig =
      for {
        analyzerConfig <- optionT(analyzerConfig)
        coordinatorConfig <- optionT(coordinatorConfig)
        sourcecodeLoaderConfig <- optionT(sourcecodeLoaderConfig)
        reporterConfig <- optionT(reporterConfig)
      } yield {
        AnalysisConfig(analyzerConfig, coordinatorConfig, sourcecodeLoaderConfig, reporterConfig)
      }
    analysisConfig.run
  }

  def loadAnalyzerConfig(data: T)(implicit ctx: ExecutionContext): Future[Option[AnalyzerConfig]] = {
    data.asYamlObject.getFields(YamlString("analyzer")) match {
      case Seq(v: YamlValue) => AnalyzerConfigYamlLoad.load(v)
      case _ =>
        logger.error(s"Failed to parse config for analyzer, $data")
        Future(None)
    }
  }

  def loadCoordinatorConfig(data: T)(implicit ctx: ExecutionContext): Future[Option[CoordinatorConfig]] = {
    data.asYamlObject.getFields(YamlString("coordinator")) match {
      case Seq(v: YamlValue) => CoordinatorConfigYamlLoad.load(v)
      case _ =>
        logger.error(s"Failed to parse config for coordinator, $data")
        Future(None)
    }
  }

  def loadSourcecodeLoaderConfig(data: T)(implicit ctx: ExecutionContext): Future[Option[SourcecodeLoaderConfig]] = {
    data.asYamlObject.getFields(YamlString("sourcecode_loader")) match {
      case Seq(v: YamlValue) => SourcecodeLoaderConfigYamlLoad.load(v)
      case _ =>
        logger.error(s"Failed to parse config for sourcecode loader, $data")
        Future(None)
    }
  }

  def loadReporterConfig(data: T)(implicit ctx: ExecutionContext): Future[Option[ReporterConfig]] = {
    data.asYamlObject.getFields(YamlString("reporter")) match {
      case Seq(v: YamlValue) => ReporterConfigYamlLoad.load(v)
      case _ =>
        logger.error(s"Failed to parse config for reporter, $data")
        Future(None)
    }
  }
}