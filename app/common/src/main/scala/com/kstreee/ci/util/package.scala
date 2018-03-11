package com.kstreee.ci

import com.typesafe.scalalogging.Logger
import play.api.libs.json.{JsError, JsResult}
import play.api.libs.ws.ahc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

package object util {
  private val logger = Logger("util")

  def lift[T](t: T)(implicit ctx: ExecutionContext): Future[Option[T]] = {
    if (t != null) {
      Future(Some(t))
    } else {
      logger.warn(s"Failed to lift data, input data is null, $getStackTrace")
      Future(None)
    }
  }

  def lift[T](t: Try[T])(implicit ctx: ExecutionContext): Future[Option[T]] = {
    Future(traceTry(t).toOption)
  }

  def lift[T](opt: Option[T])(implicit ctx: ExecutionContext): Future[Option[T]] = {
    Future(opt)
  }

  def lift(data: Future[StandaloneAhcWSRequest#Response])(implicit ctx: ExecutionContext): Future[Option[StandaloneAhcWSRequest#Response]] = {
    data.flatMap(r => Future(Some(r)))
  }

  def lift[T](r: JsResult[T])(implicit ctx: ExecutionContext): Future[Option[T]] = {
    Future(traceJsResult(r).asOpt)
  }

  def liftFailedWhenZero[T](exitValue: Int)(implicit t: Future[Option[T]], ctx: ExecutionContext): Future[Option[T]] = {
    if (exitValue == 0) t else Future(None)
  }

  def getStackTrace: String = {
    Thread.currentThread().getStackTrace.toString
  }

  def getStackTrace(t: Throwable): String = {
    t.getStackTrace.toString
  }

  def info[T](d: T): T = {
    logger.info(s"Tracing : $d")
    d
  }

  def trace[T](d: T): T = {
    logger.trace(s"Tracing : $d")
    d
  }

  def traceTry[T](x: Try[T]): Try[T] = {
    x match {
      case Failure(e) =>
        logger.error(s"Failed to execute", e)
        x
      case _ => x
    }
  }

  def traceJsResult[T](x: JsResult[T]): JsResult[T] = {
    x match {
      case JsError(e) =>
        logger.error(s"Failed to execute", e)
        x
      case _ => x
    }
  }

  def callWhenFailed[T](x: Future[Option[T]])(implicit f: () => Unit, ctx: ExecutionContext): Future[Option[T]] = {
    val closer =
      x.map { o =>
        if (o.isEmpty) {
          f()
          o
        } else {
          o
        }
      }
    closer.failed.foreach { r =>
      logger.error(s"Failed to handle future, $r")
      f()
    }
    closer
  }
}