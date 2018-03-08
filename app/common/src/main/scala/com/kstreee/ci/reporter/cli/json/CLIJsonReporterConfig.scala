package com.kstreee.ci.reporter.cli.json

import com.kstreee.ci.reporter.ReporterConfig

case class CLIJsonReporterConfig() extends ReporterConfig {
  override val name: String = "cli_json"
}