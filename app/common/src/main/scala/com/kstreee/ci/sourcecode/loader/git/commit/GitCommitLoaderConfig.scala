package com.kstreee.ci.sourcecode.loader.git.commit

import com.kstreee.ci.sourcecode.loader.git.GitLoaderConfig

case class GitCommitLoaderConfig(override val uri: String,
                                 override val sourcePath: Option[String],
                                 commitHash: String) extends GitLoaderConfig {
  override val name: String = "git_commit"
}