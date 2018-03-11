package com.kstreee.ci.sourcecode.loader

import com.kstreee.ci.analysis.Config
import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloaderConfig

trait SourcecodeLoaderConfig extends Config {
  val sourcecodeUnloaderConfig: Option[SourcecodeUnloaderConfig]
}