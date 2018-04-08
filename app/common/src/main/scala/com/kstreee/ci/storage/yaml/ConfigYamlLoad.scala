package com.kstreee.ci.storage.yaml

import com.kstreee.ci.util._
import com.kstreee.ci.storage.ConfigLoad
import com.typesafe.scalalogging.Logger
import net.jcazevedo.moultingyaml.{YamlString, YamlValue}

import scala.concurrent.{ExecutionContext, Future}

trait ConfigYamlLoad extends ConfigLoad {
  private val logger: Logger = Logger[this.type]

  override type T = YamlValue

  override def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]] = {
    val config =
      for {
        yamlValue <- data.asYamlObject.fields.get(YamlString("name"))
        name <- castToString(yamlValue)
        config <- loadConfigByName(name, data)
      } yield config
    lift(config)
  }

  private[yaml] def castToString(value: YamlValue): Option[String] = {
    value match {
      case (n: YamlString) => Some(n.value)
      case _ =>
        logger.error(s"Failed to get yamlstring from yamlvalue $value, type of value : ${value.getClass}")
        None
    }
  }

  def loadConfigByName(name: String, data: T): Option[U]
}