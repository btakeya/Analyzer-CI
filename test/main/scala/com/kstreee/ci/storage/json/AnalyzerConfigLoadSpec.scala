package com.kstreee.ci.storage.json

import com.kstreee.ci.analyzer.AnalyzerConfig
import com.kstreee.ci.analyzer.pylint.PylintAnalyzerConfig
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class AnalyzerConfigLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "pylint analyzer reads" should  {
    "valid pylint analyzer json" in {
      val name = AnalyzerConfigLoad.pylintName
      val analysisCmd = Seq("1", "2", "3")
      val json = s"""
                    |{
                    |  "name": "$name",
                    |  "analysis_cmd": "${analysisCmd.mkString(" ")}"
                    |}
      """.stripMargin
      (for {
        result <- AnalyzerConfigLoad.load(Json.parse(json))
      } yield {
        result must beSome[AnalyzerConfig]
        result.get must anInstanceOf[PylintAnalyzerConfig]
        result.get.asInstanceOf[PylintAnalyzerConfig].analysisCmd must containTheSameElementsAs(analysisCmd)
      }).await
    }
  }
}
