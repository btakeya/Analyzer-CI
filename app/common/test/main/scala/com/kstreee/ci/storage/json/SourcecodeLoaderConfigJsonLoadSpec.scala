package com.kstreee.ci.storage.json

import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig
import com.kstreee.ci.sourcecode.loader.fs.FileSystemSourcecodeLoaderConfig
import com.kstreee.ci.sourcecode.loader.git.branch.GitBranchLoaderConfig
import com.kstreee.ci.sourcecode.loader.git.commit.GitCommitLoaderConfig
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class SourcecodeLoaderConfigJsonLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "file system reads" should {
    "valid file system json" in {
      val name = SourcecodeLoaderConfigJsonLoad.fileSystemName
      val sourcePath = "TEST_SOURCE_PATH"
      val tmpPath = "TEST_TMP_PATH"
      val json = s"""
                    |{
                    |  "name": "$name",
                    |  "source_path": "$sourcePath"
                    |}
      """.stripMargin
      (for {
        result <- SourcecodeLoaderConfigJsonLoad.load(Json.parse(json))
      } yield {
        result must beSome[SourcecodeLoaderConfig]
        result.get must anInstanceOf[FileSystemSourcecodeLoaderConfig]
        result.get.asInstanceOf[FileSystemSourcecodeLoaderConfig].sourcePath mustEqual sourcePath
      }).await
    }
  }

  "git branch reads" should {
    "valid git branch json" in {
      val name = SourcecodeLoaderConfigJsonLoad.gitBranchName
      val uri = "TES_URI"
      val sourcePath = "TEST_SOURCE_PATH"
      val tmpPath = "TEST_TMP_PATH"
      val branch = "TEST_BRANCH"
      val json = s"""
                    |{
                    |  "name": "$name",
                    |  "uri": "$uri",
                    |  "source_path": "$sourcePath",
                    |  "tmp_path": "$tmpPath",
                    |  "branch": "$branch"
                    |}
      """.stripMargin
      (for  {
        result <- SourcecodeLoaderConfigJsonLoad.load(Json.parse(json))
      } yield {
        result must beSome[SourcecodeLoaderConfig]
        result.get must anInstanceOf[GitBranchLoaderConfig]
        result.get.asInstanceOf[GitBranchLoaderConfig].uri mustEqual uri
        result.get.asInstanceOf[GitBranchLoaderConfig].sourcePath must beSome[String]
        result.get.asInstanceOf[GitBranchLoaderConfig].sourcePath.get mustEqual sourcePath
        result.get.asInstanceOf[GitBranchLoaderConfig].branch mustEqual branch
      }).await
    }
  }

  "git commit reads" should {
    "valid git commit json" in {
      val name = SourcecodeLoaderConfigJsonLoad.gitCommitName
      val uri = "TES_URI"
      val tmpPath = "TEST_TMP_PATH"
      val commitHash = "TEST_COMMIT_HASH"
      val json = s"""
                    |{
                    |  "name": "$name",
                    |  "uri": "$uri",
                    |  "tmp_path": "$tmpPath",
                    |  "commit_hash": "$commitHash"
                    |}
      """.stripMargin
      (for {
        result <- SourcecodeLoaderConfigJsonLoad.load(Json.parse(json))
      } yield {
        result must beSome[SourcecodeLoaderConfig]
        result.get must anInstanceOf[GitCommitLoaderConfig]
        result.get.asInstanceOf[GitCommitLoaderConfig].uri mustEqual uri
        result.get.asInstanceOf[GitCommitLoaderConfig].commitHash mustEqual commitHash
      }).await
    }
  }
}