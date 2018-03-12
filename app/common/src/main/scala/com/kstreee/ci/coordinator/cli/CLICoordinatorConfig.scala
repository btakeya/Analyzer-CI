package com.kstreee.ci.coordinator.cli

import com.kstreee.ci.coordinator.CoordinatorConfig

case class CLICoordinatorConfig(override val timeoutSeconds: Option[Int]) extends CoordinatorConfig {
  override val name: String = "cli"
}