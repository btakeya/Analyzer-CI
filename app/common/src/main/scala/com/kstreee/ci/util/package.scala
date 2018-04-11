package com.kstreee.ci

import com.typesafe.scalalogging.Logger
import net.jcazevedo.moultingyaml._
import play.api.libs.json.{JsError, JsResult}
import play.api.libs.ws.ahc._
import net.jcazevedo.moultingyaml._

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

  def yamlToString(x: YamlValue, f: Throwable => Unit): Option[String] = {
    x match {
      case (s: YamlString) => asOption(s.value, (str: String) => str != null && !str.isEmpty, f)
      case (n: YamlNumber) => asOption(n.value.toString, (str: String) => str != null && !str.isEmpty, f)
      case _ =>
        f(new Exception(s"Failed to get string from yaml value, $x"))
        None
    }
  }

  def yamlValueByKey(value: YamlValue, key: YamlValue, f: Throwable => Unit): Option[YamlValue] = {
    asOption(value.asYamlObject.fields.get(key), f).flatten
  }

  // Helper method to interop with Java
  def parseYaml(data: String): YamlValue = {
    data.parseYaml
  }

  def yamlMap(value: YamlValue, mapper: (String => String)): YamlValue = {
    value match {
      case (set: YamlSet) =>
        logger.info(s"map set, $set")
        YamlSet(set.set.map(yamlMap(_, mapper)))
      case (lst: YamlArray) =>
        logger.info(s"map arr, $lst")
        YamlArray(lst.elements.map(yamlMap(_, mapper)))
      case (obj: YamlObject) =>
        logger.info(s"map map, $obj")
        YamlObject(obj.fields.map { case (k, v) => (k, yamlMap(v, mapper)) })
      case YamlString(str) =>
        logger.info(s"map str to str, $str -> ${mapper(str)}")
        YamlString(mapper(str))
      case _ => value
    }
  }
}