package com.kstreee.ci.coordinator.file

import com.kstreee.ci.coordinator.CoordinatorConfig

case class FileCoordinatorConfig(reportPath: String) extends CoordinatorConfig {
  override val name: String = "file"
  override val timeoutSeconds: Option[Int] = None
}