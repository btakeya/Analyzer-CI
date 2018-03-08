package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.storage.ConfigLoad
import play.api.libs.json.{JsPath, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object AnalysisConfigLoad extends ConfigLoad {
  override type T = JsValue
  override type U = AnalysisConfig
  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    (for {
      analyzerConfigData <- optionT(lift((JsPath \ "analyzer").asSingleJsResult(data)))
      analyzerConfig <- optionT(AnalyzerConfigLoad.load(analyzerConfigData))

      coordinatorConfigData <- optionT(lift((JsPath \ "coordinator").asSingleJsResult(data)))
      coordinatorConfig <- optionT(CoordinatorConfigLoad.load(coordinatorConfigData))

      sourcecodeLoaderConfigData <- optionT(lift((JsPath \ "sourcecode_loader").asSingleJsResult(data)))
      sourcecodeLoaderConfig <- optionT(SourcecodeLoaderConfigLoad.load(sourcecodeLoaderConfigData))

      reporterConfigData <- optionT(lift(traceJsResult((JsPath \ "reporter").asSingleJsResult(data))))
      reporterConfig <- optionT(ReporterConfigLoad.load(reporterConfigData))
    } yield {
      AnalysisConfig(analyzerConfig, coordinatorConfig, sourcecodeLoaderConfig, reporterConfig)
    }).run
  }
}