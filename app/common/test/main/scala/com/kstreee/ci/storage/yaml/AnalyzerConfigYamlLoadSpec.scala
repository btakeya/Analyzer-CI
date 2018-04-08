package com.kstreee.ci.storage.yaml

import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzerConfig, PylintAnalyzerReportFormat}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import net.jcazevedo.moultingyaml._

class AnalyzerConfigYamlLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "isValidJson" should {
    "return true for a valid yaml" in {
      val validYaml = s"""
                         |name: hi
      """.stripMargin
      AnalyzerConfigYamlLoad.isValidYaml(validYaml) mustEqual true
    }
    "return false for a valid yaml" in {
      val invalidYaml = s"""
                         |{"hihi": ["hye", jij]1223}
      """.stripMargin
      AnalyzerConfigYamlLoad.isValidYaml(invalidYaml) mustEqual false
    }
  }
  "pylint analyzer reads" should {
    "valid pylint analyzer yaml" in {
      val name = AnalyzerConfigYamlLoad.pylintName
      val analysisCmd = Seq("1", "2", "3")
      val reportFormat = "json"
      val yaml =
        s"""
           |name: $name
           |analysis_cmd: ${analysisCmd.mkString(" ")}
           |report_format: $reportFormat
      """.stripMargin
      (for {
        result <- AnalyzerConfigYamlLoad.load(yaml.parseYaml)
      } yield {
        result must beSome[AnalyzerConfig]
        result.get must anInstanceOf[PylintAnalyzerConfig]
        result.get.asInstanceOf[PylintAnalyzerConfig].analysisCmd must containTheSameElementsAs(analysisCmd)
        result.get.asInstanceOf[PylintAnalyzerConfig].reportFormat mustEqual PylintAnalyzerReportFormat.Json
      }).await
    }
  }
}