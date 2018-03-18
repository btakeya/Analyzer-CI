package com.kstreee.ci.common

import org.specs2.mutable.BeforeAfter

import scala.concurrent.ExecutionContext

class ActorUtilsTestContext()(implicit ctx: ExecutionContext) extends BeforeAfter {
  def before: Any = ActorUtils.initAhcActor()
  def after: Any = ActorUtils.destroyAhcActor()
}