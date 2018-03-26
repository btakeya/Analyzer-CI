package com.kstreee.ci

import com.typesafe.scalalogging.Logger
import play.api.libs.json.{JsError, JsPath, JsResult, JsonValidationError}
import play.api.libs.ws.ahc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
    Future(asOption(t, (th: Throwable) => logger.info(s"Failed to lift 'Try' has been failed.", th)))
  }

  def lift[T](opt: Option[T])(implicit ctx: ExecutionContext): Future[Option[T]] = {
    Future(opt)
  }

  def lift(data: Future[StandaloneAhcWSRequest#Response])(implicit ctx: ExecutionContext): Future[Option[StandaloneAhcWSRequest#Response]] = {
    data.flatMap(r => Future(Some(r)))
  }

  def lift[T](r: JsResult[T])(implicit ctx: ExecutionContext): Future[Option[T]] = {
    Future(asOption(r, (e: JsError) => logger.error(s"Failed to lisft, JsResult haven't been succeeded.", e)))
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

  def trace[T](x: T, logging: => Unit): T = {
    logging
    x
  }

  def tap[T](x: Option[T], f: => Unit): Option[T] = {
    if (x.isEmpty) f
    x
  }

  def tap[T](x: Try[T], f: Throwable => Unit): Try[T] = {
    if (x.isFailure) x.failed.foreach(f)
    x
  }

  def tap[T](x: JsResult[T], f: JsError => Unit): JsResult[T] = {
    x match {
      case e: JsError => f(e)
      case _ => ()
    }
    x
  }

  def asOption[T](x: => T, f: Throwable => Unit): Option[T] = {
    Try(x) match {
      case Success(v) =>
        if (v == null) {
          f(new Exception("value can't be null."))
          None
        } else {
          Some(v)
        }
      case Failure(th) => {
        f(th)
        None
      }
    }
  }

  def asOption[T](x: => T, cond: T => Boolean, f: Throwable => Unit): Option[T] = {
    asOption(x, f).flatMap { v =>
      if (cond(v)) Some(v)
      else {
        f(new Exception(s"Failed to pass condition, $v"))
        None
      }
    }
  }

  def asOption[T](x: Try[T], f: Throwable => Unit): Option[T] = {
    tap(x, f).toOption
  }

  def asOption[T](x: JsResult[T], f: JsError => Unit): Option[T] = {
    tap(x, f).asOpt
  }
}