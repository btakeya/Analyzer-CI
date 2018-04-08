package com.kstreee.ci.storage.json

import com.kstreee.ci.coordinator.CoordinatorConfig
import com.kstreee.ci.coordinator.cli.CLICoordinatorConfig
import com.kstreee.ci.coordinator.file.FileCoordinatorConfig
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class CoordinatorConfigLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "cli coordinator reads" should  {
    "valid cli coordinator json" in {
      val name = CoordinatorConfigLoad.cliName
      val timeoutSeconds = 123
      val json = s"""
                    |{
                    |  "name": "$name",
                    |  "timeout_seconds": $timeoutSeconds
                    |}
      """.stripMargin
      (for {
        result <- CoordinatorConfigLoad.load(Json.parse(json))
      } yield {
        result must beSome[CoordinatorConfig]
        result.get must anInstanceOf[CLICoordinatorConfig]
        result.get.asInstanceOf[CLICoordinatorConfig].timeoutSeconds must beSome(timeoutSeconds)
      }).await
    }

    "valid cli coordinator json with empty timeout" in {
      val name = CoordinatorConfigLoad.cliName
      val timeoutSeconds = 123
      val json = s"""
                    |{
                    |  "name": "$name"
                    |}
      """.stripMargin
      (for {
        result <- CoordinatorConfigLoad.load(Json.parse(json))
      } yield {
        result must beSome[CoordinatorConfig]
        result.get must anInstanceOf[CLICoordinatorConfig]
        result.get.asInstanceOf[CLICoordinatorConfig].timeoutSeconds must beNone
      }).await
    }
  }

  "file coordinator reads" should {
    "valid cli coordinator json" in {
      val name = CoordinatorConfigLoad.fileName
      val reportPath = "ABCD"
      val json =
        s"""
           |{
           |  "name": "$name",
           |  "report_path": "$reportPath"
           |}
      """.stripMargin
      (for {
        result <- CoordinatorConfigLoad.load(Json.parse(json))
      } yield {
        result must beSome[CoordinatorConfig]
        result.get must anInstanceOf[FileCoordinatorConfig]
        result.get.asInstanceOf[FileCoordinatorConfig].reportPath mustEqual reportPath
      }).await
    }
  }
}