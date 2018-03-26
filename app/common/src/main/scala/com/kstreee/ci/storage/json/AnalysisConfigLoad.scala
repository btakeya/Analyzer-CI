package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.coordinator.CoordinatorConfig
import com.kstreee.ci.reporter.ReporterConfig
import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig
import com.kstreee.ci.storage.ConfigLoad
import play.api.libs.json.JsPath

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object AnalysisConfigLoad extends ConfigLoad {
  override type U = AnalysisConfig

  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = loadWithDefault(data, None, None, None, None)

  def loadWithDefault(data: T,
                      analyzerConfigDefault: Option[AnalyzerConfig] = None,
                      coordinatorConfigDefault: Option[CoordinatorConfig] = None,
                      sourcecodeLoaderConfigDefault: Option[SourcecodeLoaderConfig] = None,
                      reporterConfigDefault: Option[ReporterConfig] = None)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    val analyzerConfig: Future[Option[AnalyzerConfig]] =
      if (analyzerConfigDefault.isDefined) lift(analyzerConfigDefault)
      else optionT(lift((JsPath \ "analyzer").asSingleJsResult(data))).flatMap(configData => optionT(AnalyzerConfigLoad.load(configData))).run

    val coordinatorConfig: Future[Option[CoordinatorConfig]] =
      if (coordinatorConfigDefault.isDefined) lift(coordinatorConfigDefault)
      else optionT(lift((JsPath \ "coordinator").asSingleJsResult(data))).flatMap(configData => optionT(CoordinatorConfigLoad.load(configData))).run

    val sourcecodeLoaderConfig: Future[Option[SourcecodeLoaderConfig]] =
      if (sourcecodeLoaderConfigDefault.isDefined) lift(sourcecodeLoaderConfigDefault)
      else optionT(lift((JsPath \ "sourcecode_loader").asSingleJsResult(data))).flatMap(configData => optionT(SourcecodeLoaderConfigLoad.load(configData))).run

    val reporterConfig: Future[Option[ReporterConfig]] =
      if (reporterConfigDefault.isDefined) lift(reporterConfigDefault)
      else optionT(lift((JsPath \ "reporter").asSingleJsResult(data))).flatMap(configData => optionT(ReporterConfigLoad.load(configData))).run

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
}