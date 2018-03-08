package com.kstreee.ci.reporter.cli.plain

import com.kstreee.ci.reporter.ReporterConfig

case class CLIPlainReporterConfig() extends ReporterConfig {
  override val name: String = "cli_plain"
}