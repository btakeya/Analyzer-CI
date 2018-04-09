package com.kstreee.ci.storage.json

import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzerConfig, PylintAnalyzerReportFormat}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class AnalyzerConfigJsonLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "isValidJson" should {
    "return true for a valid json" in {
      val validJson = s"""
                    |{
                    |  "name": "hi"
                    |}
      """.stripMargin
      AnalyzerConfigJsonLoad.isValidJson(validJson) mustEqual true
    }
    "return false for a valid json" in {
      val invalidJson = s"""
                         |{
                         |  "name": "hi",
                         |}
      """.stripMargin
      AnalyzerConfigJsonLoad.isValidJson(invalidJson) mustEqual false
    }
  }
  "pylint analyzer reads" should {
    "valid pylint analyzer json" in {
      val name = AnalyzerConfigJsonLoad.pylintName
      val analysisCmd = Seq("1", "2", "3")
      val reportFormat = "json"
      val json = s"""
                    |{
                    |  "name": "$name",
                    |  "analysis_cmd": "${analysisCmd.mkString(" ")}",
                    |  "report_format": "$reportFormat"
                    |}
      """.stripMargin
      (for {
        result <- AnalyzerConfigJsonLoad.load(Json.parse(json))
      } yield {
        result must beSome[AnalyzerConfig]
        result.get must anInstanceOf[PylintAnalyzerConfig]
        result.get.asInstanceOf[PylintAnalyzerConfig].analysisCmd must containTheSameElementsAs(analysisCmd)
        result.get.asInstanceOf[PylintAnalyzerConfig].reportFormat mustEqual PylintAnalyzerReportFormat.Json
      }).await
    }
  }
}