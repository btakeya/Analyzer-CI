package com.kstreee.ci.sourcecode.unloader.fs

import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloaderConfig

case class FileSystemSourcecodeUnloaderConfig(path: String) extends SourcecodeUnloaderConfig {
  override val name: String = "file_system_symbolic"
}