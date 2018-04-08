package com.kstreee.ci.storage.yaml

import com.kstreee.ci.coordinator.CoordinatorConfig
import com.kstreee.ci.coordinator.cli.CLICoordinatorConfig
import com.kstreee.ci.coordinator.file.FileCoordinatorConfig
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import net.jcazevedo.moultingyaml._

class CoordinatorConfigYamlLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "cli coordinator reads" should {
    "valid cli coordinator yaml" in {
      val name = CoordinatorConfigYamlLoad.cliName
      val timeoutSeconds = 123
      val yaml = s"""
                    |name: $name
                    |timeout_seconds: $timeoutSeconds
      """.stripMargin
      (for {
        result <- CoordinatorConfigYamlLoad.load(yaml.parseYaml)
      } yield {
        result must beSome[CoordinatorConfig]
        result.get must anInstanceOf[CLICoordinatorConfig]
        result.get.asInstanceOf[CLICoordinatorConfig].timeoutSeconds must beSome(timeoutSeconds)
      }).await
    }

    "valid cli coordinator json with empty timeout" in {
      val name = CoordinatorConfigYamlLoad.cliName
      val timeoutSeconds = 123
      val yaml = s"""
                    |name: $name
      """.stripMargin
      (for {
        result <- CoordinatorConfigYamlLoad.load(yaml.parseYaml)
      } yield {
        result must beSome[CoordinatorConfig]
        result.get must anInstanceOf[CLICoordinatorConfig]
        result.get.asInstanceOf[CLICoordinatorConfig].timeoutSeconds must beNone
      }).await
    }
  }

  "file coordinator reads" should {
    "valid cli coordinator yaml" in {
      val name = CoordinatorConfigYamlLoad.fileName
      val reportPath = "ABCD"
      val yaml =
        s"""
           |name: $name
           |report_path: $reportPath
      """.stripMargin
      (for {
        result <- CoordinatorConfigYamlLoad.load(yaml.parseYaml)
      } yield {
        result must beSome[CoordinatorConfig]
        result.get must anInstanceOf[FileCoordinatorConfig]
        result.get.asInstanceOf[FileCoordinatorConfig].reportPath mustEqual reportPath
      }).await
    }
  }
}
