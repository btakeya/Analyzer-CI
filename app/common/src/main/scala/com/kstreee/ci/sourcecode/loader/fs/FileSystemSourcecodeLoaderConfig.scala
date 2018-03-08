package com.kstreee.ci.sourcecode.loader.fs

import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig
import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloaderConfig

final case class FileSystemSourcecodeLoaderConfig(sourcePath: String,
                                                  override val sourcecodeUnloaderConfig: Option[SourcecodeUnloaderConfig] = None) extends SourcecodeLoaderConfig {
  override val name: String = "file_system"
}