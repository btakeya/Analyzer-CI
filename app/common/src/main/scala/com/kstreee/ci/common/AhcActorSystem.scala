package com.kstreee.ci.common

import com.kstreee.ci.util._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import play.api.libs.ws.ahc.{AhcWSClientConfigFactory, StandaloneAhcWSClient}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, SECONDS}
import scala.util.Try

case class AhcActorSystem(classLoader: ClassLoader = Thread.currentThread().getContextClassLoader)
                         (implicit ctx: ExecutionContext) {
  private val logger: Logger = Logger[this.type]

  private val ahcActorSystem: Option[ActorSystem] = {
    asOption(
      Try(ActorSystem("ahc", ConfigFactory.load(classLoader), classLoader)),
      (th: Throwable) => logger.error(s"Failed to initialize actor system.", th))
  }

  private val ahcActorMaterializer = {
    ahcActorSystem.flatMap { actor =>
      asOption(
        Try(ActorMaterializer()(actor)),
        (th: Throwable) => logger.error(s"Failed to initialize actor materializer.", th))
    }
  }

  def toOption: Option[AhcActorSystem] = if (ahcActorSystem.isDefined && ahcActorMaterializer.isDefined) Some(this) else None

  def destroy(): Unit = {
    this.ahcActorMaterializer.foreach { materializer => if (!materializer.isShutdown) materializer.shutdown() else () }
    this.ahcActorSystem.foreach { actor =>
      val termination = actor.terminate()
      termination.foreach(_ => logger.info("Cleaning up AHC Actor System."))
      termination.failed.foreach(e => logger.error(s"Failed to clean up AHC Actor System, $e", e))
    }
  }

  def getWSClient: Option[StandaloneAhcWSClient] = {
    for {
      materializer <- ahcActorMaterializer
      defaultConfig <- asOption(
        Try(AhcWSClientConfigFactory.forConfig(ConfigFactory.load(classLoader), classLoader)),
        (th: Throwable) => logger.error(s"Failed to load ahc client config.", th))
      ahcConfig <- asOption(
        Try(defaultConfig.copy(maxRequestRetry = 2, wsClientConfig = defaultConfig.wsClientConfig.copy(idleTimeout = Duration(10, SECONDS)))),
        (th: Throwable) => logger.error(s"Failed to copy default config from ahc client config.", th))
    } yield {
      StandaloneAhcWSClient(ahcConfig)(materializer)
    }
  }
}