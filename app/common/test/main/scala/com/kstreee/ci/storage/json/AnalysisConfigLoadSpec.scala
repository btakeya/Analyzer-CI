package com.kstreee.ci.storage.json

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.analyzer.pylint.PylintAnalyzerConfig
import com.kstreee.ci.coordinator.cli.CLICoordinatorConfig
import com.kstreee.ci.reporter.cli.plain.CLIPlainReporterConfig
import com.kstreee.ci.sourcecode.loader.fs.FileSystemSourcecodeLoaderConfig
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class AnalysisConfigLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "analysis reads" should  {
    "valid analysis json" in {
      // PylintAnalyzer
      val analyzerName = AnalyzerConfigLoad.pylintName
      val analysisCmd = Seq("1", "2", "3")
      // CLICoordinator
      val coordinatorName = CoordinatorConfigLoad.cliName
      val timeoutSeconds = 123
      // FileSystemSymbolic
      val sourcecodeLoaderName = SourcecodeLoaderConfigLoad.fileSystemName
      val sourcePath = "TEST_SOURCE_PATH"
      // CLIReporter
      val reporterName = ReporterConfigLoad.cliPlainName
      val json = s"""
               |{
               |  "analyzer": {
               |    "name": "$analyzerName",
               |    "analysis_cmd": "${analysisCmd.mkString(" ")}"
               |  },
               |  "coordinator": {
               |    "name": "$coordinatorName",
               |    "timeout_seconds": $timeoutSeconds
               |  },
               |  "sourcecode_loader": {
               |    "name": "$sourcecodeLoaderName",
               |    "source_path": "$sourcePath"
               |  },
               |  "reporter": {
               |    "name": "$reporterName"
               |  }
               |}
      """.stripMargin

      (for {
        result <- AnalysisConfigLoad.load(Json.parse(json))
      } yield {
        result must beSome[AnalysisConfig]

        result.get.analyzerConfig must anInstanceOf[PylintAnalyzerConfig]
        result.get.analyzerConfig.asInstanceOf[PylintAnalyzerConfig].analysisCmd must containTheSameElementsAs(analysisCmd)

        result.get.coordinatorConfig must anInstanceOf[CLICoordinatorConfig]
        result.get.coordinatorConfig.asInstanceOf[CLICoordinatorConfig].timeoutSeconds mustEqual timeoutSeconds

        result.get.sourcecodeLoaderConfig must anInstanceOf[FileSystemSourcecodeLoaderConfig]
        result.get.sourcecodeLoaderConfig.asInstanceOf[FileSystemSourcecodeLoaderConfig].sourcePath mustEqual sourcePath

        result.get.reporterConfig must anInstanceOf[CLIPlainReporterConfig]
      }).await
    }
  }
}