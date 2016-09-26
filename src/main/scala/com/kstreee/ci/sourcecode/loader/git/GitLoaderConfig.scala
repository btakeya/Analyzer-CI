package com.kstreee.ci.sourcecode.loader.git

import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig
import com.kstreee.ci.sourcecode.unloader.SourcecodeUnloaderConfig

trait GitLoaderConfig extends SourcecodeLoaderConfig {
  val uri: String
  val sourcePath: Option[String]

  // Set unloader based on source path.
  // If the source path isn't none, it will generate symbolic link, but the path is none, it will clone from git.
  lazy val basePath: Option[String] = createTmpDirectoryIfNotExists(sourcePath)
  override lazy val sourcecodeUnloaderConfig: Option[SourcecodeUnloaderConfig] = basePath.flatMap(fileSystemUnloadIfNotExists(sourcePath, _))
}