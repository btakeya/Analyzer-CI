package com.kstreee.ci.storage

import scala.concurrent.{ExecutionContext, Future}

trait ConfigLoad {
  type T
  type U
  def load(data: T)(implicit ctx: ExecutionContext): Future[Option[U]]
}