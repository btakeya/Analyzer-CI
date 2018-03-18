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

object ActorUtils {
  private val logger = Logger[this.type]

  var classLoader: Option[ClassLoader] = None
  var ahcActorSystem: Option[ActorSystem] = None
  var ahcActorMaterializer: Option[ActorMaterializer] = None

  def initAhcActor(classLoader: ClassLoader = Thread.currentThread().getContextClassLoader)
                  (implicit ctx: ExecutionContext): Boolean = {
    this.classLoader = Some(classLoader)
    this.ahcActorSystem = callWhenFailed(traceTry(Try(ActorSystem("ahc", ConfigFactory.load(classLoader), classLoader))).toOption) { () =>
      logger.error("Failed to initialize actor system")
    }
    this.ahcActorMaterializer = this.ahcActorSystem.flatMap { actor =>
      callWhenFailed(traceTry(Try(ActorMaterializer()(actor))).toOption) { () =>
        logger.error("Failed to initialize actor materializer")
      }
    }
    this.ahcActorSystem.isDefined && this.ahcActorMaterializer.isDefined
  }

  def destroyAhcActor()(implicit ctx: ExecutionContext): Unit = {
    this.ahcActorMaterializer.foreach { materializer =>
      materializer.shutdown()
    }
    this.ahcActorSystem.foreach { actor =>
      val termination = actor.terminate()
      termination.foreach(_ => logger.info("Cleaning up AHC Actor System."))
      termination.failed.foreach(e => logger.error(s"Failed to clean up AHC Actor System, $e", e))
    }
  }

  def getWSClient: Option[StandaloneAhcWSClient] = {
    for {
      classLoader <- this.classLoader
      materializer <- ahcActorMaterializer
      defaultConfig <- traceTry(Try(AhcWSClientConfigFactory.forConfig(ConfigFactory.load(classLoader), classLoader))).toOption
      ahcConfig <- traceTry(Try(defaultConfig.copy(
        maxRequestRetry = 2, wsClientConfig = defaultConfig.wsClientConfig.copy(idleTimeout = Duration(10, SECONDS))))).toOption
    } yield {
      StandaloneAhcWSClient(ahcConfig)(materializer)
    }
  }
}