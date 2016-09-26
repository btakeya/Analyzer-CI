package com.kstreee.ci.sourcecode.loader.git.branch

import com.kstreee.ci.sourcecode.loader.git.GitLoaderConfig

case class GitBranchLoaderConfig(override val uri: String,
                                 override val sourcePath: Option[String],
                                 branch: String) extends GitLoaderConfig {
  override val name: String = "git_branch"
}