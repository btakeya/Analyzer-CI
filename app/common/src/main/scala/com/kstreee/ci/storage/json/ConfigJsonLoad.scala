package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.storage.ConfigLoad
import com.typesafe.scalalogging.Logger
import play.api.libs.json.{JsError, JsPath, JsResult, JsValue}

import scala.concurrent.{ExecutionContext, Future}

trait ConfigJsonLoad extends ConfigLoad {
  private val logger: Logger = Logger[this.type]

  override type T = JsValue

  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    val config =
      for {
        name <- (JsPath \ "name").read[String].reads(data)
        config <- loadConfigByName(name, data)
      } yield config
    Future(asOption(config, (e: JsError) => logger.error("Failed to parse config,", e)))
  }

  def loadConfigByName(name: String, data: T): JsResult[U]
}
