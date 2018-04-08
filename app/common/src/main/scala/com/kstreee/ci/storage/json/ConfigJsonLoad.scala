package com.kstreee.ci.storage.json

import com.kstreee.ci.util._
import com.kstreee.ci.storage.ConfigLoad
import com.typesafe.scalalogging.Logger
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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

  // To support Java
  def loadByString(data: String)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    if (isValidJson(data)) {
      load(Json.parse(data))
    } else {
      logger.error(s"Failed to parse data as Json, $data")
      Future(None)
    }
  }

  def isValidJson(data: String): Boolean = {
    Try(Json.parse(data)) match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  def loadConfigByName(name: String, data: T): JsResult[U]
}
