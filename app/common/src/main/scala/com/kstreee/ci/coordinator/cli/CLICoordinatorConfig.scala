package com.kstreee.ci.coordinator.cli

import com.kstreee.ci.coordinator.CoordinatorConfig

case class CLICoordinatorConfig(timeoutSeconds: Int) extends CoordinatorConfig {
  override val name: String = "cli"
}