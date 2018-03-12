package com.kstreee.ci.storage

import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

trait ConfigLoad {
  type T = JsValue
  type U
  def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]]
}