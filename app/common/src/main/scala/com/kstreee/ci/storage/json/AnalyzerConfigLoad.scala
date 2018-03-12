package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.analyzer.checkstyle.CheckstyleAnalyzerConfig
import com.kstreee.ci.analyzer.pylint.PylintAnalyzerConfig
import com.kstreee.ci.storage.ConfigLoad
import com.typesafe.scalalogging.Logger
import play.api.libs.json.{JsError, JsPath, JsResult, Reads}

import scala.concurrent.{ExecutionContext, Future}

object AnalyzerConfigLoad extends ConfigLoad {
  private val logger = Logger[this.type]

  override type U = AnalyzerConfig
  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    val config =
      for {
        name <- (JsPath \ "name").read[String].reads(data)
        config <- loadConfigByName(name, data)
      } yield config
    Future(traceJsResult(config).asOpt)
  }

  private[json] val pylintName: String = "pylint"
  private[json] implicit val pylintReads: Reads[AnalyzerConfig] =
    (JsPath \ "analysis_cmd").read[String].map[Seq[String]](s => s.trim.split("\\s+")).map(PylintAnalyzerConfig.apply)
  private[json] val checkstyleName: String = "pylint"
  private[json] implicit val checkstyleReads: Reads[AnalyzerConfig] =
    (JsPath \ "analysis_cmd").read[String].map[Seq[String]](s => s.trim.split("\\s+")).map(CheckstyleAnalyzerConfig.apply)

  private[json] def loadConfigByName(name: String, data: T): JsResult[U] = {
    name match {
      case _ if pylintName.equalsIgnoreCase(name) => pylintReads.reads(data)
      case _ if checkstyleName.equalsIgnoreCase(name) => checkstyleReads.reads(data)
      case _ =>
        logger.warn(s"Not implemented, $name")
        JsError(s"Not implemented, $name")
    }
  }
}