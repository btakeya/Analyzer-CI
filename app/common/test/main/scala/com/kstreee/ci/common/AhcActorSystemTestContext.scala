package com.kstreee.ci.common

import org.specs2.mutable.BeforeAfter

import scala.concurrent.ExecutionContext

class AhcActorSystemTestContext(ahcActorSystem: AhcActorSystem)(implicit ctx: ExecutionContext) extends BeforeAfter {
  def before: Any = Unit
  def after: Any = ahcActorSystem.destroy()
}